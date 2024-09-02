package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallbackFactory;
import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig{

    @Bean
    public RequestInterceptor requestInterceptor(){
        return template -> {
            Long user = UserContext.getUser();
            if(user != null){
                template.header("user-info", user.toString());
            }
        };
    }

    @Bean
    public ItemClientFallbackFactory itemClientFallbackFactory(){
        return new ItemClientFallbackFactory();
    }
}
