package com.gym.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 测试数据库连接
 */
@Controller
public class TestController {

    private final JdbcTemplate jdbc;

    public TestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/test/db")
    @ResponseBody
    public String testDatabase() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 数据库连接测试 ===\n\n");

        try {
            // 测试 1: 连接测试
            sb.append("1. 连接测试: ");
            Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
            sb.append(result == 1 ? "✓ 成功\n" : "✗ 失败\n");

            // 测试 2: 会员数量
            sb.append("2. 会员数量: ");
            Integer memberCount = jdbc.queryForObject("SELECT COUNT(*) FROM member", Integer.class);
            sb.append(memberCount).append(" 条\n");

            // 测试 3: 教练数量
            sb.append("3. 教练数量: ");
            Integer coachCount = jdbc.queryForObject("SELECT COUNT(*) FROM coach", Integer.class);
            sb.append(coachCount).append(" 条\n");

            // 测试 4: 课程数量
            sb.append("4. 课程数量: ");
            Integer courseCount = jdbc.queryForObject("SELECT COUNT(*) FROM course", Integer.class);
            sb.append(courseCount).append(" 条\n");

            // 测试 5: 查询会员数据
            sb.append("5. 会员数据样例:\n");
            List<Map<String, Object>> members = jdbc.queryForList("SELECT member_id, name, phone FROM member LIMIT 3");
            for (Map<String, Object> m : members) {
                sb.append("   - ").append(m.get("member_id"))
                  .append(", ").append(m.get("name"))
                  .append(", ").append(m.get("phone")).append("\n");
            }

            sb.append("\n=== 测试完成 ===");

        } catch (Exception e) {
            sb.append("\n✗ 测试失败: ").append(e.getMessage()).append("\n");
            sb.append("异常类型: ").append(e.getClass().getName()).append("\n");
        }

        return sb.toString();
    }
}
