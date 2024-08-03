package org.entropy.smslogin.config;

import jakarta.annotation.Resource;
import org.entropy.smslogin.interceptor.LoginInterceptor;
import org.entropy.smslogin.interceptor.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 使用order控制拦截器拦截顺序，order值相对越小，优先级相对越高
        registry.addInterceptor(refreshTokenInterceptor).order(0); // 默认拦截所有请求
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/webjars/**",
                        "/doc.html/**",
                        "/v3/api-docs/**"
                ).order(1);
    }
}
