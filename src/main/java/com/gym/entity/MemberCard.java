package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 会员卡表 (会员 1:n)
 */
@Entity
@Table(name = "member_card")
public class MemberCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Integer cardId;

    @Column(name = "member_id", nullable = false, length = 10)
    private String memberId;

    @Column(name = "card_type", nullable = false, length = 10)
    private String cardType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_times", nullable = false)
    private Integer totalTimes;

    @Column(name = "remaining_times", nullable = false)
    private Integer remainingTimes;

    @Column(length = 10)
    private String status;

    // Getters & Setters
    public Integer getCardId() { return cardId; }
    public void setCardId(Integer cardId) { this.cardId = cardId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getTotalTimes() { return totalTimes; }
    public void setTotalTimes(Integer totalTimes) { this.totalTimes = totalTimes; }
    public Integer getRemainingTimes() { return remainingTimes; }
    public void setRemainingTimes(Integer remainingTimes) { this.remainingTimes = remainingTimes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
