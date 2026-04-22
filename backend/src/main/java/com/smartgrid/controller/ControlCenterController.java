package com.smartgrid.controller;

import com.smartgrid.center.ControlCenterService;
import com.smartgrid.dto.AggregationRequest;
import com.smartgrid.dto.StatisticsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/center")
public class ControlCenterController {

    @Autowired
    private ControlCenterService controlCenterService;

    @PostMapping("/aggregate")
    public ResponseEntity<StatisticsResponse> receiveAggregation(@RequestBody AggregationRequest request) {
        try {
            StatisticsResponse response = controlCenterService.processAggregation(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<StatisticsResponse>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(controlCenterService.getStatistics(startTime, endTime));
    }
}
