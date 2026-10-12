package com.gym.controller;

import com.gym.entity.MemberCard;
import com.gym.entity.Reservation;
import com.gym.repository.MemberCardRepository;
import com.gym.repository.ReservationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 预约管理：列表 / 新增（冲突检测）/ 签到 / 消课 / 取消
 */
@Controller
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final MemberCardRepository cardRepository;
    private final JdbcTemplate jdbc;

    public ReservationController(ReservationRepository reservationRepository,
                                 MemberCardRepository cardRepository,
                                 JdbcTemplate jdbc) {
        this.reservationRepository = reservationRepository;
        this.cardRepository = cardRepository;
        this.jdbc = jdbc;
    }

    /** 列表（按角色过滤 + 日期筛选 + 动作处理） */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String date,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) Integer id,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model) {
        String role = (String) session.getAttribute("role");
        String refId = (String) session.getAttribute("refId");

        // 处理动作：签到/取消/消课
        if (action != null && id != null) {
            handleAction(action, id);
            String query = "?page=" + Math.max(page, 0);
            if (date != null && !date.isEmpty()) query += "&date=" + date;
            return "redirect:/reservation/list" + query;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT r.*, m.name AS mname, c.name AS cname, co.course_name, co.duration " +
                "FROM reservation r JOIN member m ON r.member_id=m.member_id " +
                "JOIN coach c ON r.coach_id=c.coach_id JOIN course co ON r.course_id=co.course_id WHERE 1=1 ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM reservation r WHERE 1=1 ");
        if (date != null && !date.isEmpty()) {
            sql.append(" AND DATE(r.reserve_time)=?");
            countSql.append(" AND DATE(r.reserve_time)=?");
        }
        if ("coach".equals(role)) {
            sql.append(" AND r.coach_id=?");
            countSql.append(" AND r.coach_id=?");
        } else if ("member".equals(role)) {
            sql.append(" AND r.member_id=?");
            countSql.append(" AND r.member_id=?");
        }
        sql.append(" ORDER BY r.reserve_time DESC LIMIT ? OFFSET ?");
        java.util.List<Object> queryArgs = new java.util.ArrayList<>();
        java.util.List<Object> countArgs = new java.util.ArrayList<>();
        if (date != null && !date.isEmpty()) { queryArgs.add(date); countArgs.add(date); }
        if ("coach".equals(role) || "member".equals(role)) { queryArgs.add(refId); countArgs.add(refId); }
        int pageNumber = Math.max(page, 0);
        int pageSize = 10;
        queryArgs.add(pageSize);
        queryArgs.add((long) pageNumber * pageSize);
        List<Map<String, Object>> reservations = jdbc.queryForList(sql.toString(), queryArgs.toArray());
        long total = jdbc.queryForObject(countSql.toString(), Long.class, countArgs.toArray());
        Page<Map<String, Object>> reservationPage = new PageImpl<>(reservations, PageRequest.of(pageNumber, pageSize), total);

        model.addAttribute("date", date == null ? "" : date);
        model.addAttribute("role", role);
        model.addAttribute("reservations", reservations);
        model.addAttribute("pageData", reservationPage);
        model.addAttribute("currentPage", reservationPage.getNumber());
        model.addAttribute("pagePath", "/reservation/list");
        return "reservation/list";
    }

    @Transactional
    public void handleAction(String action, int id) {
        Reservation r = reservationRepository.findById(id).orElse(null);
        if (r == null) return;
        switch (action) {
            case "signin" -> {
                r.setStatus("已签到");
                reservationRepository.save(r);
            }
            case "cancel" -> {
                r.setStatus("已取消");
                reservationRepository.save(r);
            }
            case "finish" -> {
                r.setStatus("已消课");
                reservationRepository.save(r);
                // 消课：扣减会员次卡剩余次数1次
                List<MemberCard> cards = cardRepository.findByMemberIdAndStatus(r.getMemberId(), "有效");
                for (MemberCard c : cards) {
                    if ("次卡".equals(c.getCardType()) && c.getRemainingTimes() > 0) {
                        c.setRemainingTimes(c.getRemainingTimes() - 1);
                        cardRepository.save(c);
                        break;
                    }
                }
            }
            default -> { }
        }
    }

    @GetMapping("/add")
    public String addPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!("admin".equals(role) || "front".equals(role) || "member".equals(role))) {
            throw new IllegalStateException("权限不足，无法访问此页面。");
        }
        model.addAttribute("coaches", jdbc.queryForList("SELECT * FROM coach WHERE status='在职'"));
        model.addAttribute("courses", jdbc.queryForList("SELECT * FROM course"));
        model.addAttribute("members", jdbc.queryForList("SELECT * FROM member WHERE status='正常'"));
        model.addAttribute("myRefId", session.getAttribute("refId"));
        return "reservation/add";
    }

    @PostMapping("/add")
    public String doAdd(@RequestParam String memberId,
                        @RequestParam String coachId,
                        @RequestParam String courseId,
                        @RequestParam String reserveTime,
                        HttpSession session,
                        Model model) {
        String role = (String) session.getAttribute("role");
        if (!("admin".equals(role) || "front".equals(role) || "member".equals(role))) {
            throw new IllegalStateException("权限不足，无法访问此页面。");
        }
        LocalDateTime rt = LocalDateTime.parse(reserveTime.replace("T", " "), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        // 1. 教练时间冲突检测
        long conflict = reservationRepository.countByCoachIdAndReserveTimeAndStatusNot(coachId, rt, "已取消");
        if (conflict > 0) {
            model.addAttribute("msg", "该教练在 " + reserveTime + " 已有课程预约，时间冲突！请选择其他时间或教练。");
            model.addAttribute("msgType", "danger");
            return addPage(session, model);
        }
        // 2. 会员有效卡检查
        long validCard = cardRepository.countByMemberIdAndStatus(memberId, "有效");
        if (validCard == 0) {
            model.addAttribute("msg", "该会员暂无有效会员卡，请先办卡。");
            model.addAttribute("msgType", "warning");
            return addPage(session, model);
        }
        // 3. 保存预约
        Reservation r = new Reservation();
        r.setMemberId(memberId);
        r.setCoachId(coachId);
        r.setCourseId(courseId);
        r.setReserveTime(rt);
        r.setStatus("已预约");
        reservationRepository.save(r);
        model.addAttribute("msg", "预约成功！");
        model.addAttribute("msgType", "success");
        return addPage(session, model);
    }
}
