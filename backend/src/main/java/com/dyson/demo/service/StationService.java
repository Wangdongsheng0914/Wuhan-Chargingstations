package com.dyson.demo.service;

import com.dyson.demo.dto.RecommendationRequest;
import com.dyson.demo.dto.StationDTO;
import com.dyson.demo.entity.Station;
import com.dyson.demo.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private BaiduMapService baiduMapService;

    // 地球半径（公里）
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 根据用户位置和条件推荐充电站
     */
    public List<StationDTO> recommendStations(RecommendationRequest request) {
        // 计算搜索范围（经纬度范围）
        // 1度纬度约111公里，1度经度在武汉约85公里
        double latRange = request.getMaxDistance() / 111.0;
        double lngRange = request.getMaxDistance() / 85.0; // 武汉地区的经度换算

        BigDecimal minLat = request.getLat().subtract(BigDecimal.valueOf(latRange));
        BigDecimal maxLat = request.getLat().add(BigDecimal.valueOf(latRange));
        BigDecimal minLng = request.getLng().subtract(BigDecimal.valueOf(lngRange));
        BigDecimal maxLng = request.getLng().add(BigDecimal.valueOf(lngRange));

        // 查询范围内的所有充电站
        List<Station> nearbyStations = stationRepository.findStationsInRange(
            minLat, maxLat, minLng, maxLng
        );

        // 转换为DTO列表，先用Haversine公式计算粗略距离进行初步筛选
        List<StationDTO> candidateStations = new ArrayList<>();
        for (Station station : nearbyStations) {
            // 先用Haversine公式计算粗略距离（用于初步筛选）
            double roughDistance = calculateDistance(
                request.getLat().doubleValue(),
                request.getLng().doubleValue(),
                station.getLat().doubleValue(),
                station.getLng().doubleValue()
            );

            // 如果粗略距离超过最大搜索半径，跳过
            if (roughDistance > request.getMaxDistance()) {
                continue;
            }

            StationDTO dto = convertToDTO(station, roughDistance);

            // 应用过滤条件
            if (passesFilters(dto, request)) {
                candidateStations.add(dto);
            }
        }

        // 直接使用候选充电站列表，距离由前端通过浏览器端路径规划API计算
        // 这样可以避免服务端频繁调用API导致的超时和并发限制问题
        List<StationDTO> stations = candidateStations;

        // 根据优先级排序
        stations = sortByPriority(stations, request.getPriority());

        // 限制返回数量
        return stations.stream()
                .limit(request.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * 使用Haversine公式计算两点之间的距离（公里）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 检查充电站是否通过过滤条件
     */
    private boolean passesFilters(StationDTO station, RecommendationRequest request) {
        // 最小功率过滤
        if (request.getMinPower() != null && station.getMaxPowerKw() != null) {
            if (station.getMaxPowerKw().compareTo(request.getMinPower()) < 0) {
                return false;
            }
        }

        // 最小可用数量过滤
        if (request.getMinAvailable() != null && station.getAvailableConnectors() != null) {
            if (station.getAvailableConnectors() < request.getMinAvailable()) {
                return false;
            }
        }

        // 充电类型过滤
        if (request.getChargingType() != null && !request.getChargingType().isEmpty()) {
            if (!request.getChargingType().equals(station.getChargingType())) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据优先级排序
     */
    private List<StationDTO> sortByPriority(List<StationDTO> stations, String priority) {
        switch (priority) {
            case "power":
                // 功率优先：按最大功率降序，然后按距离升序
                return stations.stream()
                        .sorted(Comparator
                                .comparing((StationDTO s) -> s.getMaxPowerKw() != null ? s.getMaxPowerKw() : BigDecimal.ZERO, Comparator.reverseOrder())
                                .thenComparing(Comparator.comparing((StationDTO s) -> s.getDistance() != null ? s.getDistance() : Double.MAX_VALUE))
                        )
                        .collect(Collectors.toList());

            case "available":
                // 可用数量优先：按可用充电桩数量降序，然后按距离升序
                return stations.stream()
                        .sorted(Comparator
                                .comparing((StationDTO s) -> s.getAvailableConnectors() != null ? s.getAvailableConnectors() : 0, Comparator.reverseOrder())
                                .thenComparing(Comparator.comparing((StationDTO s) -> s.getDistance() != null ? s.getDistance() : Double.MAX_VALUE))
                        )
                        .collect(Collectors.toList());

            case "distance":
            default:
                // 距离优先：按距离升序
                return stations.stream()
                        .sorted(Comparator.comparing((StationDTO s) -> s.getDistance() != null ? s.getDistance() : Double.MAX_VALUE))
                        .collect(Collectors.toList());
        }
    }

    /**
     * 将Entity转换为DTO
     */
    private StationDTO convertToDTO(Station station, Double distance) {
        StationDTO dto = new StationDTO();
        dto.setId(station.getId());
        dto.setName(station.getName());
        dto.setAddress(station.getAddress());
        dto.setLat(station.getLat());
        dto.setLng(station.getLng());
        dto.setOperator(station.getOperator());
        dto.setChargingType(station.getChargingType());
        dto.setServiceHours(station.getServiceHours());
        dto.setIs24h(station.getIs24h());
        dto.setTotalConnectors(station.getTotalConnectors());
        dto.setAvailableConnectors(station.getAvailableConnectors());
        dto.setMaxPowerKw(station.getMaxPowerKw());
        dto.setParkingSpots(station.getParkingSpots());
        dto.setDistance(distance);
        return dto;
    }
}
