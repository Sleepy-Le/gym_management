package com.gym.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘：按角色展示不同内容
 */
@Controller
public class DashboardController {

    private final JdbcTemplate jdbc;

    public DashboardController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        String refId = (String) session.getAttribute("refId");
        model.addAttribute("today", LocalDate.now());

        if ("admin".equals(role) || "front".equals(role)) {
            // 统计卡片
            model.addAttribute("memberCount",
                    jdbc.queryForObject("SELECT COUNT(*) FROM member WHERE status='正常'", Integer.class));
            model.addAttribute("coachCount",
                    jdbc.queryForObject("SELECT COUNT(*) FROM coach WHERE status='在职'", Integer.class));
            model.addAttribute("todayReserve",
                    jdbc.queryForObject("SELECT COUNT(*) FROM reservation WHERE DATE(reserve_time)=CURDATE()", Integer.class));
            model.addAttribute("monthRevenue",
                    jdbc.queryForObject("SELECT COALESCE(SUM(total_amount),0) FROM sale_order WHERE MONTH(order_date)=MONTH(CURDATE()) AND YEAR(order_date)=YEAR(CURDATE())", java.math.BigDecimal.class));
            // 今日预约
            List<Map<String, Object>> todayReservations = jdbc.queryForList(
                    "SELECT r.*, m.name AS mname, c.name AS cname, co.course_name, co.duration " +
                    "FROM reservation r JOIN member m ON r.member_id=m.member_id " +
                    "JOIN coach c ON r.coach_id=c.coach_id JOIN course co ON r.course_id=co.course_id " +
                    "WHERE DATE(r.reserve_time)=CURDATE() ORDER BY r.reserve_time");
            model.addAttribute("todayReservations", todayReservations);
            // 库存预警
            model.addAttribute("warnProducts",
                    jdbc.queryForList("SELECT * FROM product WHERE stock <= warn_threshold ORDER BY stock"));
        } else if ("coach".equals(role)) {
            model.addAttribute("todayCourse",
                    jdbc.queryForObject("SELECT COUNT(*) FROM reservation WHERE coach_id=? AND DATE(reserve_time)=CURDATE()", Integer.class, refId));
            model.addAttribute("monthClass",
                    jdbc.queryForObject("SELECT COUNT(*) FROM reservation WHERE coach_id=? AND status='已消课' AND MONTH(reserve_time)=MONTH(CURDATE())", Integer.class, refId));
            model.addAttribute("hourlyRate",
                    jdbc.queryForObject("SELECT hourly_rate FROM coach WHERE coach_id=?", java.math.BigDecimal.class, refId));
            model.addAttribute("mySchedule", jdbc.queryForList(
                    "SELECT r.*, m.name AS mname, co.course_name FROM reservation r " +
                    "JOIN member m ON r.member_id=m.member_id JOIN course co ON r.course_id=co.course_id " +
                    "WHERE r.coach_id=? AND r.reserve_time>=CURDATE() ORDER BY r.reserve_time LIMIT 10", refId));
        } else {
            // 会员
            Map<String, Object> card = jdbc.queryForMap(
                    "SELECT COUNT(*) AS cardCount, COALESCE(SUM(remaining_times),0) AS totalRemaining, MAX(end_date) AS latestExpire " +
                    "FROM member_card WHERE member_id=? AND status='有效'", refId);
            model.addAttribute("cardSummary", card);
            model.addAttribute("myReservations", jdbc.queryForList(
                    "SELECT r.*, c.name AS cname, co.course_name FROM reservation r " +
                    "JOIN coach c ON r.coach_id=c.coach_id JOIN course co ON r.course_id=co.course_id " +
                    "WHERE r.member_id=? AND r.reserve_time>=CURDATE() ORDER BY r.reserve_time LIMIT 5", refId));
            model.addAttribute("myBody", jdbc.queryForList(
                    "SELECT * FROM body_measure WHERE member_id=? ORDER BY measure_date DESC LIMIT 6", refId));
        }
        return "dashboard";
    }
}
