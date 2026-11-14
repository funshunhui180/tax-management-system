package com.example.tax.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.tax.common.RoleConstants;
import com.example.tax.entity.User;
import com.example.tax.interceptor.LoginInterceptor;
import com.example.tax.service.UserService;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 登录提交接口
     */
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim())
                .eq(User::getPassword, password);
        User user = userService.getOne(wrapper);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "账号或密码错误");
            return "redirect:/login";
        }

        // 登录成功，将用户信息存入 Session（含 id、username、role、taxpayerId）
        session.setAttribute(LoginInterceptor.LOGIN_USER, user);

        // 按角色跳转：管理员 -> 纳税人管理；审核员/纳税人 -> 申报管理
        if (user.getRole() != null && user.getRole() == RoleConstants.ADMIN) {
            return "redirect:/taxpayer/list";
        }
        return "redirect:/declaration/list";
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
