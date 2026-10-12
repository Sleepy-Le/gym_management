package com.gym.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 体测记录表
 */
@Entity
@Table(name = "body_measure")
public class BodyMeasure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measure_id")
    private Integer measureId;

    @Column(name = "member_id", nullable = false, length = 10)
    private String memberId;

    @Column(name = "measure_date", nullable = false)
    private LocalDate measureDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "body_fat", precision = 4, scale = 1)
    private BigDecimal bodyFat;

    @Column(precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(precision = 5, scale = 1)
    private BigDecimal chest;

    @Column(precision = 5, scale = 1)
    private BigDecimal waist;

    @Column(precision = 5, scale = 1)
    private BigDecimal hip;

    @Column(columnDefinition = "TEXT")
    private String remark;

    // Getters & Setters
    public Integer getMeasureId() { return measureId; }
    public void setMeasureId(Integer measureId) { this.measureId = measureId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public LocalDate getMeasureDate() { return measureDate; }
    public void setMeasureDate(LocalDate measureDate) { this.measureDate = measureDate; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getBodyFat() { return bodyFat; }
    public void setBodyFat(BigDecimal bodyFat) { this.bodyFat = bodyFat; }
    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }
    public BigDecimal getChest() { return chest; }
    public void setChest(BigDecimal chest) { this.chest = chest; }
    public BigDecimal getWaist() { return waist; }
    public void setWaist(BigDecimal waist) { this.waist = waist; }
    public BigDecimal getHip() { return hip; }
    public void setHip(BigDecimal hip) { this.hip = hip; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
