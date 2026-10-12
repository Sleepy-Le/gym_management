package com.gym.controller;

import com.gym.entity.UserAccount;
import com.gym.repository.UserAccountRepository;
import com.gym.util.MD5Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 登录 / 退出
 */
@Controller
public class AuthController {

    private final UserAccountRepository userRepository;

    public AuthController(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        String md5 = MD5Util.md5(password);
        UserAccount user = userRepository.findByUsernameAndPasswordAndStatus(username, md5, "正常");
        if (user == null) {
            model.addAttribute("error", "用户名或密码错误，或账号已被禁用");
            return "login";
        }
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("realName", user.getRealName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("refId", user.getRefId());
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
