package com.kemiel.cakeordering.admin.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 攔截器註冊設定
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminSessionInterceptor adminSessionInterceptor;

    /**
     * 攔截所有 /api/admin/** 路徑，登入／登出本身排除在外
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminSessionInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login", "/api/admin/logout");
    }
}