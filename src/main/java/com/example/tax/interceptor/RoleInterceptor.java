package com.example.tax.interceptor;

import com.example.tax.annotation.RequireRoles;
import com.example.tax.common.RoleConstants;
import com.example.tax.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 角色权限拦截器：RBAC 的后端兜底。
 * <p>校验顺序：</p>
 * <ol>
 *   <li>URL 规则：对纳税人档案管理（{@code /taxpayer/**}）做强制限制，
 *       即使 Controller 忘记加注解也无法越权；</li>
 *   <li>{@link RequireRoles} 注解：方法级 / 类级声明式权限控制。</li>
 * </ol>
 * <p>无权限时返回 403，由 {@code templates/error/403.html} 渲染统一提示页。</p>
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RoleInterceptor.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final UrlPathHelper PATH_HELPER = new UrlPathHelper();

    /**
     * URL 级权限规则（兜底）。
     * <p>{@code /taxpayer/**} 全部限定为管理员：</p>
     * <ul>
     *   <li>纳税人(2) 访问 {@code /taxpayer/list} 会被拦截，防止看到他人的企业信息；</li>
     *   <li>审核员(1) 访问 {@code /taxpayer/add}、{@code /taxpayer/edit/**}、
     *       {@code /taxpayer/save}、{@code /taxpayer/delete/**} 会被拦截。</li>
     * </ul>
     */
    private static final Map<String, int[]> URL_RULES;

    static {
        Map<String, int[]> rules = new LinkedHashMap<>();
        rules.put("/taxpayer", new int[]{RoleConstants.ADMIN});
        rules.put("/taxpayer/**", new int[]{RoleConstants.ADMIN});
        URL_RULES = Collections.unmodifiableMap(rules);
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(LoginInterceptor.LOGIN_USER);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String path = PATH_HELPER.getPathWithinApplication(request);

        // 1) URL 兜底规则：不依赖 Controller 是否标注解，防止新增接口漏配权限
        int[] urlRoles = findUrlRoles(path);
        if (urlRoles != null && !hasAnyRole(user, urlRoles)) {
            return deny(request, response, user, path, "URL 规则");
        }

        // 2) 注解规则：非 Controller 方法（静态资源等）无需校验
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRoles requireRoles = handlerMethod.getMethodAnnotation(RequireRoles.class);
        if (requireRoles == null) {
            requireRoles = handlerMethod.getBeanType().getAnnotation(RequireRoles.class);
        }
        if (requireRoles == null) {
            return true;
        }

        if (hasAnyRole(user, requireRoles.value())) {
            return true;
        }
        return deny(request, response, user, path, "@RequireRoles");
    }

    private int[] findUrlRoles(String path) {
        for (Map.Entry<String, int[]> entry : URL_RULES.entrySet()) {
            if (PATH_MATCHER.match(entry.getKey(), path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean hasAnyRole(User user, int[] allowedRoles) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        for (int role : allowedRoles) {
            if (role == user.getRole()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拒绝访问并返回 403 提示页（HTTP 状态码保持 403，便于日志与安全审计）。
     */
    private boolean deny(HttpServletRequest request, HttpServletResponse response,
                         User user, String path, String rule) throws IOException {
        log.warn("拦截越权访问: username={}, role={}, method={}, path={}, rule={}",
                user.getUsername(), user.getRole(), request.getMethod(), path, rule);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问该资源");
        return false;
    }
}
