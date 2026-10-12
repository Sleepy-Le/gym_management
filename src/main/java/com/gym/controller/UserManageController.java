package com.gym.controller;

import com.gym.entity.UserAccount;
import com.gym.repository.UserAccountRepository;
import com.gym.util.MD5Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * 系统管理：用户权限
 */
@Controller
@RequestMapping("/system")
public class UserManageController {

    private final UserAccountRepository userRepository;

    public UserManageController(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public String userPage(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin");
        Page<UserAccount> userPage = userRepository.findAllByOrderByUserIdAsc(PageRequest.of(Math.max(page, 0), 10));
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("pageData", userPage);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("pagePath", "/system/user");
        return "system/user";
    }

    /** 启用/禁用 */
    @GetMapping("/user/toggle")
    public String toggle(@RequestParam int id, HttpSession session) {
        requireRole(session, "admin");
        userRepository.findById(id).ifPresent(u -> {
            u.setStatus("正常".equals(u.getStatus()) ? "禁用" : "正常");
            userRepository.save(u);
        });
        return "redirect:/system/user";
    }

    /** 重置密码为 123456 */
    @GetMapping("/user/reset")
    public String reset(@RequestParam int id, HttpSession session) {
        requireRole(session, "admin");
        userRepository.findById(id).ifPresent(u -> {
            u.setPassword(MD5Util.md5("123456"));
            userRepository.save(u);
        });
        return "redirect:/system/user";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
