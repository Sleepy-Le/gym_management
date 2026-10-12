package com.gym.controller;

import com.gym.entity.Product;
import com.gym.entity.SaleItem;
import com.gym.entity.SaleOrder;
import com.gym.repository.ProductRepository;
import com.gym.repository.SaleItemRepository;
import com.gym.repository.SaleOrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费管理：列表 / 新增（主从表，自动扣库存）
 */
@Controller
@RequestMapping("/sale")
public class SaleController {

    private final SaleOrderRepository orderRepository;
    private final SaleItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final JdbcTemplate jdbc;

    public SaleController(SaleOrderRepository orderRepository,
                          SaleItemRepository itemRepository,
                          ProductRepository productRepository,
                          JdbcTemplate jdbc) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.jdbc = jdbc;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        Page<SaleOrder> orderPage = orderRepository.findAllByOrderByOrderDateDesc(PageRequest.of(Math.max(page, 0), 10));
        List<Map<String, Object>> orders = orderPage.getContent().stream().map(order -> jdbc.queryForMap(
                "SELECT s.*, m.name AS mname FROM sale_order s LEFT JOIN member m ON s.member_id=m.member_id " +
                "WHERE s.order_id=?", order.getOrderId())).toList();
        // 为每个订单拼接商品明细字符串
        for (Map<String, Object> order : orders) {
            List<Map<String, Object>> items = jdbc.queryForList(
                    "SELECT i.quantity, p.name FROM sale_item i JOIN product p ON i.product_id=p.product_id " +
                    "WHERE i.order_id=? ORDER BY i.item_id", order.get("order_id"));
            StringBuilder sb = new StringBuilder();
            for (Map<String, Object> it : items) {
                sb.append(it.get("name")).append("×").append(it.get("quantity")).append("，");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            order.put("items", sb.toString());
        }
        model.addAttribute("orders", orders);
        model.addAttribute("pageData", orderPage);
        model.addAttribute("currentPage", orderPage.getNumber());
        model.addAttribute("pagePath", "/sale/list");
        return "sale/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        model.addAttribute("members", jdbc.queryForList("SELECT * FROM member WHERE status='正常'"));
        model.addAttribute("products", productRepository.findByStockGreaterThan(0));
        return "sale/add";
    }

    @PostMapping("/add")
    @Transactional
    public String doAdd(@RequestParam(required = false) String memberId,
                        @RequestParam String payMethod,
                        @RequestParam("productId") List<String> productIds,
                        @RequestParam("quantity") List<Integer> quantities,
                        HttpSession session) {
        requireRole(session, "admin", "front");
        BigDecimal total = BigDecimal.ZERO;
        SaleOrder order = new SaleOrder();
        order.setMemberId(memberId == null || memberId.isEmpty() ? null : memberId);
        order.setOperator((String) session.getAttribute("realName"));
        order.setOrderDate(LocalDateTime.now());
        order.setPayMethod(payMethod);
        orderRepository.save(order);

        for (int i = 0; i < productIds.size(); i++) {
            String pid = productIds.get(i);
            int qty = quantities.get(i);
            if (pid == null || pid.isEmpty() || qty <= 0) continue;
            Product p = productRepository.findById(pid).orElse(null);
            if (p == null || p.getStock() < qty) continue;
            BigDecimal subtotal = p.getSalePrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);
            SaleItem item = new SaleItem();
            item.setOrderId(order.getOrderId());
            item.setProductId(pid);
            item.setQuantity(qty);
            item.setUnitPrice(p.getSalePrice());
            item.setSubtotal(subtotal);
            itemRepository.save(item);
            // 自动扣库存
            p.setStock(p.getStock() - qty);
            productRepository.save(p);
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
        return "redirect:/sale/list";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
