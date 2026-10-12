package com.gym.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计报表
 */
@Controller
@RequestMapping("/report")
public class ReportController {

    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        requireRole(session, "admin");
        model.addAttribute("todayRevenue",
                jdbc.queryForObject("SELECT COALESCE(SUM(total_amount),0) FROM sale_order WHERE DATE(order_date)=CURDATE()", BigDecimal.class));
        Map<String, Object> month = jdbc.queryForMap(
                "SELECT COALESCE(SUM(total_amount),0) AS s, COUNT(*) AS c FROM sale_order WHERE MONTH(order_date)=MONTH(CURDATE()) AND YEAR(order_date)=YEAR(CURDATE())");
        model.addAttribute("monthRevenue", month.get("s"));
        model.addAttribute("monthCount", month.get("c"));
        model.addAttribute("activeMember",
                jdbc.queryForObject("SELECT COUNT(*) FROM member WHERE status='正常'", Integer.class));
        model.addAttribute("monthClass",
                jdbc.queryForObject("SELECT COUNT(*) FROM reservation WHERE MONTH(reserve_time)=MONTH(CURDATE()) AND status='已消课'", Integer.class));
        // 教练提成
        model.addAttribute("coachStats", jdbc.queryForList(
                "SELECT c.name, c.specialty, c.hourly_rate, COUNT(r.reservation_id) AS cnt " +
                "FROM coach c LEFT JOIN reservation r ON c.coach_id=r.coach_id AND r.status='已消课' " +
                "AND MONTH(r.reserve_time)=MONTH(CURDATE()) GROUP BY c.coach_id ORDER BY cnt DESC"));
        // 商品销售排行
        model.addAttribute("topProducts", jdbc.queryForList(
                "SELECT p.name, SUM(i.quantity) AS qty, SUM(i.subtotal) AS amt FROM sale_item i " +
                "JOIN product p ON i.product_id=p.product_id GROUP BY p.product_id ORDER BY qty DESC LIMIT 8"));
        // 近7日营收
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            BigDecimal v = jdbc.queryForObject(
                    "SELECT COALESCE(SUM(total_amount),0) FROM sale_order WHERE DATE(order_date)=?",
                    BigDecimal.class, d);
            labels.add(d.toString());
            data.add(v);
        }
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);
        return "report/index";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
