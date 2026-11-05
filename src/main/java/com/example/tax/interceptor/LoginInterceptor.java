package com.example.tax.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute(LOGIN_USER) != null) {
            return true;
        }
        // 未登录，强制跳转到登录页（相对当前应用上下文）
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
