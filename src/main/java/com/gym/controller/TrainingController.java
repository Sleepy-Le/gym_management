package com.gym.controller;

import com.gym.entity.TrainingDetail;
import com.gym.entity.TrainingLog;
import com.gym.repository.TrainingDetailRepository;
import com.gym.repository.TrainingLogRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 训练管理：列表 / 录入（主从表）/ 详情
 */
@Controller
@RequestMapping("/training")
public class TrainingController {

    private final TrainingLogRepository logRepository;
    private final TrainingDetailRepository detailRepository;
    private final JdbcTemplate jdbc;

    public TrainingController(TrainingLogRepository logRepository,
                              TrainingDetailRepository detailRepository,
                              JdbcTemplate jdbc) {
        this.logRepository = logRepository;
        this.detailRepository = detailRepository;
        this.jdbc = jdbc;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        String refId = (String) session.getAttribute("refId");
        Page<TrainingLog> logPage;
        if ("coach".equals(role)) {
            logPage = logRepository.findByCoachIdOrderByTrainingDateDesc(refId, PageRequest.of(Math.max(page, 0), 10));
        } else if ("member".equals(role)) {
            logPage = logRepository.findByMemberIdOrderByTrainingDateDesc(refId, PageRequest.of(Math.max(page, 0), 10));
        } else {
            logPage = logRepository.findAllByOrderByTrainingDateDesc(PageRequest.of(Math.max(page, 0), 10));
        }
        List<Map<String, Object>> logs = logPage.getContent().stream().map(log -> jdbc.queryForMap(
                "SELECT t.*, m.name AS mname, c.name AS cname FROM training_log t " +
                "JOIN member m ON t.member_id=m.member_id LEFT JOIN coach c ON t.coach_id=c.coach_id " +
                "WHERE t.log_id=?", log.getLogId())).toList();
        model.addAttribute("logs", logs);
        model.addAttribute("pageData", logPage);
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("pagePath", "/training/list");
        return "training/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (!("admin".equals(role) || "coach".equals(role))) {
            throw new IllegalStateException("权限不足，无法访问此页面。");
        }
        model.addAttribute("members", jdbc.queryForList("SELECT * FROM member WHERE status='正常'"));
        model.addAttribute("coaches", jdbc.queryForList("SELECT * FROM coach WHERE status='在职'"));
        model.addAttribute("role", role);
        model.addAttribute("myRefId", session.getAttribute("refId"));
        return "training/add";
    }

    @PostMapping("/add")
    @Transactional
    public String doAdd(@RequestParam String memberId,
                        @RequestParam(required = false) String coachId,
                        @RequestParam String trainingDate,
                        @RequestParam String trainingPart,
                        @RequestParam(defaultValue = "60") int duration,
                        @RequestParam(required = false) String remark,
                        @RequestParam("exerciseName") List<String> exerciseNames,
                        @RequestParam("weight") List<BigDecimal> weights,
                        @RequestParam("sets") List<Integer> setsList,
                        @RequestParam("reps") List<Integer> repsList,
                        HttpSession session) {
        String role = (String) session.getAttribute("role");
        String refId = (String) session.getAttribute("refId");
        String cid = "coach".equals(role) ? refId : coachId;
        int totalSets = setsList.stream().mapToInt(Integer::intValue).sum();

        TrainingLog log = new TrainingLog();
        log.setMemberId(memberId);
        log.setCoachId(cid);
        log.setTrainingDate(LocalDate.parse(trainingDate));
        log.setTrainingPart(trainingPart);
        log.setTotalSets(totalSets);
        log.setDuration(duration);
        log.setRemark(remark);
        logRepository.save(log);

        for (int i = 0; i < exerciseNames.size(); i++) {
            String name = exerciseNames.get(i);
            if (name == null || name.trim().isEmpty()) continue;
            TrainingDetail d = new TrainingDetail();
            d.setLogId(log.getLogId());
            d.setExerciseName(name.trim());
            d.setWeight(weights.get(i));
            d.setSets(setsList.get(i));
            d.setReps(repsList.get(i));
            d.setRestSeconds(60);
            detailRepository.save(d);
        }
        return "redirect:/training/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam int id, Model model) {
        TrainingLog log = logRepository.findById(id).orElseThrow();
        Map<String, Object> logInfo = jdbc.queryForMap(
                "SELECT t.*, m.name AS mname, c.name AS cname FROM training_log t " +
                "JOIN member m ON t.member_id=m.member_id LEFT JOIN coach c ON t.coach_id=c.coach_id " +
                "WHERE t.log_id=?", id);
        model.addAttribute("log", log);
        model.addAttribute("logInfo", logInfo);
        model.addAttribute("details", detailRepository.findByLogIdOrderByDetailIdAsc(id));
        return "training/detail";
    }
}
