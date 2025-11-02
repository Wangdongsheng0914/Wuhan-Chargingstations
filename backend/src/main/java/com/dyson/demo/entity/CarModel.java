package com.dyson.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cars_with_images")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;

    private String manufacturer;

    @Column(name = "electric_range_km")
    private Integer electricRangeKm;

    @Column(name = "charging_time_hours")
    private String chargingTimeHours;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getElectricRangeKm() {
        return electricRangeKm;
    }

    public void setElectricRangeKm(Integer electricRangeKm) {
        this.electricRangeKm = electricRangeKm;
    }

    public String getChargingTimeHours() {
        return chargingTimeHours;
    }

    public void setChargingTimeHours(String chargingTimeHours) {
        this.chargingTimeHours = chargingTimeHours;
    }
}
