package com.example.tax.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 业务层拒绝访问异常。
 * <p>由 Spring MVC 的 {@code ResponseStatusExceptionResolver} 转换为 403 响应，
 * 再由 {@code templates/error/403.html} 渲染统一的无权访问提示页。</p>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
