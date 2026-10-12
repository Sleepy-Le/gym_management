package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 训练记录表 (主表)
 */
@Entity
@Table(name = "training_log")
public class TrainingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "member_id", nullable = false, length = 10)
    private String memberId;

    @Column(name = "coach_id", length = 10)
    private String coachId;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_part", nullable = false, length = 20)
    private String trainingPart;

    @Column(name = "total_sets", nullable = false)
    private Integer totalSets;

    @Column(nullable = false)
    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String remark;

    // Getters & Setters
    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getCoachId() { return coachId; }
    public void setCoachId(String coachId) { this.coachId = coachId; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }
    public String getTrainingPart() { return trainingPart; }
    public void setTrainingPart(String trainingPart) { this.trainingPart = trainingPart; }
    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
