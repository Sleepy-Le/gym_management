package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 预约单表 (会员/教练/课程 多外键)
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @Column(name = "member_id", nullable = false, length = 10)
    private String memberId;

    @Column(name = "coach_id", nullable = false, length = 10)
    private String coachId;

    @Column(name = "course_id", nullable = false, length = 10)
    private String courseId;

    @Column(name = "reserve_time", nullable = false)
    private LocalDateTime reserveTime;

    @Column(length = 10)
    private String status;

    // Getters & Setters
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getCoachId() { return coachId; }
    public void setCoachId(String coachId) { this.coachId = coachId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public LocalDateTime getReserveTime() { return reserveTime; }
    public void setReserveTime(LocalDateTime reserveTime) { this.reserveTime = reserveTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
