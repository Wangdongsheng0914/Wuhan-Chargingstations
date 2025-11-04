package com.dyson.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    @NotNull(message = "纬度不能为空")
    private BigDecimal lat;
    
    @NotNull(message = "经度不能为空")
    private BigDecimal lng;
    
    private String priority = "distance"; // 优先级：distance(距离优先), power(功率优先), available(可用数量优先)
    
    private Double maxDistance = 10.0; // 最大搜索半径（公里），默认10公里
    
    private Integer limit = 20; // 返回结果数量，默认20个
    
    private BigDecimal minPower = null; // 最小功率要求（可选）
    
    private Integer minAvailable = null; // 最小可用充电桩数量（可选）
    
    private String chargingType = null; // 充电类型过滤（fast, super_fast等）
}
