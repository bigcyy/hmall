package com.hmall.common.interceptors;

import com.hmall.common.utils.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 从 threadLocal 中清除用户信息
        UserContext.removeUser();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1 从请求头中获取用户信息
        String userInfo = request.getHeader("user-info");
        // 2 判断是否存在用户信息，存在则存入 threadLocal
        if(userInfo != null && !userInfo.isEmpty()) {
            UserContext.setUser(Long.valueOf(userInfo));
        }
        // 3 放行
        return true;
    }
}
