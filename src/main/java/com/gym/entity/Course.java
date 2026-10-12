package com.gym.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 课程表
 */
@Entity
@Table(name = "course")
public class Course {

    @Id
    @Column(name = "course_id", length = 10)
    private String courseId;

    @Column(name = "course_name", nullable = false, length = 30)
    private String courseName;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Getters & Setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
