package com.dyson.demo.repository;

import com.dyson.demo.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CarImageRepository extends JpaRepository<CarImage, Long> {
    Optional<CarImage> findByCarName(String carName);

    interface CarImageInfo {
        Long getId();
        String getCarName();
    }

    @Query("SELECT c.id as id, c.carName as carName FROM CarImage c")
    List<CarImageInfo> findAllCarImageInfo();
}
