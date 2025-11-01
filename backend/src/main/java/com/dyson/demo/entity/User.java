package com.dyson.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(length = 10)
    private String id;  // 格式: CS + 5位随机数字 (例如: CS12345)

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;  // BCrypt 加密后的密码

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "car_model", length = 100)
    private String carModel;  // 电动汽车车型（例如：小米SU7plus）

    @Column(name = "battery_level")
    private Integer batteryLevel;  // 剩余电量 0-100%

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

