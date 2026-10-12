package com.gym.controller;

import com.gym.entity.Course;
import com.gym.repository.CourseRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 课程管理
 */
@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin");
        Page<Course> coursePage = courseRepository.findAllByOrderByCourseIdAsc(PageRequest.of(Math.max(page, 0), 10));
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("pageData", coursePage);
        model.addAttribute("currentPage", coursePage.getNumber());
        model.addAttribute("pagePath", "/course/list");
        return "course/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session) {
        requireRole(session, "admin");
        return "course/add";
    }

    @PostMapping("/add")
    public String doAdd(@RequestParam String courseName,
                        @RequestParam String type,
                        @RequestParam int duration,
                        @RequestParam int capacity,
                        @RequestParam BigDecimal price,
                        @RequestParam(required = false) String description,
                        HttpSession session) {
        requireRole(session, "admin");
        Course c = new Course();
        c.setCourseId("K" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 100)));
        c.setCourseName(courseName);
        c.setType(type);
        c.setDuration(duration);
        c.setCapacity(capacity);
        c.setPrice(price);
        c.setDescription(description);
        courseRepository.save(c);
        return "redirect:/course/list";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
