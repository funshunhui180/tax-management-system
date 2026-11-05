package com.example.tax.config;

import com.example.tax.interceptor.LoginInterceptor;
import com.example.tax.interceptor.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final RoleInterceptor roleInterceptor;

    public WebMvcConfig(LoginInterceptor loginInterceptor,
                        RoleInterceptor roleInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] excludes = {"/login", "/doLogin", "/logout", "/error",
                "/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico"};

        // 顺序很重要：先校验登录，再校验角色
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }
}
