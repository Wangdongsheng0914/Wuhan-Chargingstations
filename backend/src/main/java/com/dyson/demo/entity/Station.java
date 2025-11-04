package com.dyson.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Station {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal lat;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal lng;

    @Column(length = 255)
    private String operator;

    @Column(name = "charging_type", length = 100)
    private String chargingType;

    @Column(name = "service_hours", length = 200)
    private String serviceHours;

    @Column(name = "is_24h")
    private Boolean is24h;

    @Column(name = "total_connectors")
    private Integer totalConnectors;

    @Column(name = "available_connectors")
    private Integer availableConnectors;

    @Column(name = "max_power_kw", precision = 8, scale = 2)
    private BigDecimal maxPowerKw;

    @Column(name = "parking_spots")
    private Integer parkingSpots;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
