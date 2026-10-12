package com.gym.controller;

import com.gym.entity.Product;
import com.gym.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 商品库存管理
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        Page<Product> productPage = productRepository.findAllByOrderByProductIdAsc(PageRequest.of(Math.max(page, 0), 10));
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("pageData", productPage);
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("pagePath", "/product/list");
        return "product/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session) {
        requireRole(session, "admin", "front");
        return "product/add";
    }

    @PostMapping("/add")
    public String doAdd(@RequestParam String name,
                        @RequestParam String category,
                        @RequestParam(defaultValue = "0") BigDecimal costPrice,
                        @RequestParam BigDecimal salePrice,
                        @RequestParam(defaultValue = "0") int stock,
                        @RequestParam(defaultValue = "5") int warnThreshold,
                        @RequestParam(defaultValue = "件") String unit,
                        HttpSession session) {
        requireRole(session, "admin", "front");
        Product p = new Product();
        p.setProductId("P" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 100)));
        p.setName(name);
        p.setCategory(category);
        p.setCostPrice(costPrice);
        p.setSalePrice(salePrice);
        p.setStock(stock);
        p.setWarnThreshold(warnThreshold);
        p.setUnit(unit);
        productRepository.save(p);
        return "redirect:/product/list";
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
