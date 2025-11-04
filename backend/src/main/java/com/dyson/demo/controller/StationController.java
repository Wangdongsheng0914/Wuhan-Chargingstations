package com.dyson.demo.controller;

import com.dyson.demo.dto.RecommendationRequest;
import com.dyson.demo.dto.StationDTO;
import com.dyson.demo.service.StationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "http://localhost:5173")
public class StationController {

    @Autowired
    private StationService stationService;

    /**
     * 充电站推荐接口
     */
    @PostMapping("/recommend")
    public ResponseEntity<?> recommendStations(@Valid @RequestBody RecommendationRequest request,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("请求参数错误: " + errorMessage);
        }

        try {
            List<StationDTO> stations = stationService.recommendStations(request);
            return ResponseEntity.ok(stations);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("服务器错误: " + e.getMessage());
        }
    }
}
