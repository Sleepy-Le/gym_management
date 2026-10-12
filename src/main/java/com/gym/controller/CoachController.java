package com.gym.controller;

import com.gym.entity.Coach;
import com.gym.entity.UserAccount;
import com.gym.repository.CoachRepository;
import com.gym.repository.UserAccountRepository;
import com.gym.util.MD5Util;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 教练管理
 */
@Controller
@RequestMapping("/coach")
public class CoachController {

    private final CoachRepository coachRepository;
    private final UserAccountRepository userRepository;
    private final JdbcTemplate jdbc;

    public CoachController(CoachRepository coachRepository,
                           UserAccountRepository userRepository,
                           JdbcTemplate jdbc) {
        this.coachRepository = coachRepository;
        this.userRepository = userRepository;
        this.jdbc = jdbc;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin");
        Page<Coach> coachPage = coachRepository.findAllByOrderByCoachIdAsc(PageRequest.of(Math.max(page, 0), 10));
        List<Coach> coaches = coachPage.getContent();
        model.addAttribute("coaches", coaches);
        model.addAttribute("pageData", coachPage);
        model.addAttribute("currentPage", coachPage.getNumber());
        model.addAttribute("pagePath", "/coach/list");
        // 本月课时
        java.util.Map<String, Integer> countMap = new java.util.HashMap<>();
        for (Coach c : coaches) {
            Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM reservation WHERE coach_id=? AND status='已消课' AND MONTH(reserve_time)=MONTH(CURDATE())",
                    Integer.class, c.getCoachId());
            countMap.put(c.getCoachId(), cnt == null ? 0 : cnt);
        }
        model.addAttribute("monthCountMap", countMap);
        return "coach/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session) {
        requireRole(session, "admin");
        return "coach/add";
    }

    @PostMapping("/add")
    public String doAdd(@RequestParam String name,
                        @RequestParam String gender,
                        @RequestParam String phone,
                        @RequestParam(required = false) String specialty,
                        @RequestParam(defaultValue = "300") BigDecimal hourlyRate,
                        HttpSession session,
                        Model model) {
        requireRole(session, "admin");
        String cid = "C" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 100));
        Coach c = new Coach();
        c.setCoachId(cid);
        c.setName(name);
        c.setGender(gender);
        c.setPhone(phone);
        c.setSpecialty(specialty);
        c.setHourlyRate(hourlyRate);
        c.setHireDate(LocalDate.now());
        c.setStatus("在职");
        coachRepository.save(c);
        // 自动创建登录账号 coach + 编号，密码 123456
        UserAccount u = new UserAccount();
        u.setUsername("coach" + cid.substring(1));
        u.setPassword(MD5Util.md5("123456"));
        u.setRole("coach");
        u.setRefId(cid);
        u.setRealName(name);
        u.setStatus("正常");
        userRepository.save(u);
        model.addAttribute("msg", "教练添加成功！登录账号：coach" + cid.substring(1) + " / 密码：123456");
        return "coach/add";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
