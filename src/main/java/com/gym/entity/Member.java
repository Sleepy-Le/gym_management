package com.gym.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 会员表
 */
@Entity
@Table(name = "member")
public class Member {

    @Id
    @Column(name = "member_id", length = 10)
    private String memberId;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    private LocalDate birthday;

    @Column(name = "register_date", nullable = false)
    private LocalDate registerDate;

    @Column(length = 10)
    private String status;

    @Column(length = 100)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String remark;

    // Getters & Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public LocalDate getRegisterDate() { return registerDate; }
    public void setRegisterDate(LocalDate registerDate) { this.registerDate = registerDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
