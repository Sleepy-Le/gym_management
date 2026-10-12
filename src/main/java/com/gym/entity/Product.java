package com.gym.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 商品表
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @Column(name = "product_id", length = 10)
    private String productId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(length = 20)
    private String category;

    @Column(name = "cost_price", precision = 8, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "warn_threshold", nullable = false)
    private Integer warnThreshold;

    @Column(length = 10)
    private String unit;

    @Column(columnDefinition = "TEXT")
    private String remark;

    // Getters & Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getWarnThreshold() { return warnThreshold; }
    public void setWarnThreshold(Integer warnThreshold) { this.warnThreshold = warnThreshold; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
