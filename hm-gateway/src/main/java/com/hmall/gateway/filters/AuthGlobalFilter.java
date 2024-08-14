package com.hmall.gateway.filters;

import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter {

    private final JwtTool jwtTool;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final AuthProperties authProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1 获取请求
        ServerHttpRequest request = exchange.getRequest();
        // 2 判断路径是否放行
        if(isExclude(request.getPath().toString())){
            // 直接放行
            return chain.filter(exchange);
        }
        // 3 从请求中获取 token
        String token = null;
        List<String> tokens = request.getHeaders().get("Authorization");
        if(tokens != null && !tokens.isEmpty()){
            token = tokens.get(0);
        }
        // 4 解析 token
        Long userId = null;
        try {
            userId = jwtTool.parseToken(token);
        }catch (UnauthorizedException e){
            // 无权限访问，修改响应码并结束流程
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 5 保存用户信息
        String userInfo = userId.toString();
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 6 放行
        return chain.filter(newExchange);
    }

    private boolean isExclude(String path) {
        if(authProperties.getExcludePaths() == null || authProperties.getExcludePaths().isEmpty()){
            return false;
        }
        return authProperties.getExcludePaths()
                .stream()
                .anyMatch((pattern) -> antPathMatcher.match(pattern,path));
    }
}
