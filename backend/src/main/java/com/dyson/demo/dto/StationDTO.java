package com.dyson.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO {
    private String id;
    private String name;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String operator;
    private String chargingType;
    private String serviceHours;
    private Boolean is24h;
    private Integer totalConnectors;
    private Integer availableConnectors;
    private BigDecimal maxPowerKw;
    private Integer parkingSpots;
    private Double distance; // 距离用户位置的距离（公里）
}
