package com.dyson.demo.repository;

import com.dyson.demo.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, String> {
    
    /**
     * 根据用户位置和半径查找附近的充电站（粗略范围查询）
     * 使用简单的经纬度范围过滤，然后在Java中精确计算距离
     * 1度纬度约111公里，1度经度在武汉约85公里
     */
    @Query(value = """
        SELECT * FROM stations
        WHERE lat BETWEEN :minLat AND :maxLat
        AND lng BETWEEN :minLng AND :maxLng
        """, nativeQuery = true)
    List<Station> findStationsInRange(@Param("minLat") BigDecimal minLat,
                                      @Param("maxLat") BigDecimal maxLat,
                                      @Param("minLng") BigDecimal minLng,
                                      @Param("maxLng") BigDecimal maxLng);
    
    /**
     * 查找所有充电站（用于测试）
     */
    List<Station> findAll();
}
