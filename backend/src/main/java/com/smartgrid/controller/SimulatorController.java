package com.smartgrid.controller;

import com.smartgrid.meter.MeterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    @Autowired
    private MeterManager meterManager;

    @PostMapping("/start")
    public ResponseEntity<?> startSimulator(@RequestBody(required = false) Map<String, List<String>> request) {
        try {
            if (request != null && request.containsKey("deviceIds")) {
                List<String> deviceIds = request.get("deviceIds");
                for (String deviceId : deviceIds) {
                    meterManager.startMeter(deviceId);
                }
            } else {
                meterManager.startAllMeters();
            }
            return ResponseEntity.ok("Simulator started");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stopSimulator(@RequestBody(required = false) Map<String, List<String>> request) {
        try {
            if (request != null && request.containsKey("deviceIds")) {
                List<String> deviceIds = request.get("deviceIds");
                for (String deviceId : deviceIds) {
                    meterManager.stopMeter(deviceId);
                }
            } else {
                meterManager.stopAllMeters();
            }
            return ResponseEntity.ok("Simulator stopped");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(meterManager.getAllMeterStatus());
    }

    @GetMapping("/status/{deviceId}")
    public ResponseEntity<Map<String, Object>> getDeviceStatus(@PathVariable String deviceId) {
        return ResponseEntity.ok(meterManager.getMeterStatus(deviceId));
    }
}
