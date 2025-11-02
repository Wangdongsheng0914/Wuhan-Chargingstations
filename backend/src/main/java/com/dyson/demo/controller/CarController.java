package com.dyson.demo.controller;

import com.dyson.demo.entity.CarImage;
import com.dyson.demo.entity.CarModel;
import com.dyson.demo.repository.CarImageRepository;
import com.dyson.demo.repository.CarModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private CarImageRepository carImageRepository;

    @GetMapping
    public List<CarModel> getAllCarModels() {
        return carModelRepository.findAll();
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImageByModel(@RequestParam String model) {
        List<CarImageRepository.CarImageInfo> allImageInfos = carImageRepository.findAllCarImageInfo();

        Optional<CarImageRepository.CarImageInfo> bestMatchInfo = allImageInfos.stream()
                .filter(imageInfo -> model.contains(imageInfo.getCarName()))
                .max(Comparator.comparing(imageInfo -> imageInfo.getCarName().length()));

        if (bestMatchInfo.isPresent()) {
            // Now fetch the single, full entity
            Optional<CarImage> imageOptional = carImageRepository.findById(bestMatchInfo.get().getId());
            if (imageOptional.isPresent()) {
                byte[] imageData = imageOptional.get().getImageData();
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageData);
            } else {
                // This case is unlikely if the info was just found, but good practice
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
