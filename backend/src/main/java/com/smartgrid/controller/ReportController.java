package com.smartgrid.controller;

import com.smartgrid.dto.ReportRequest;
import com.smartgrid.fog.FogNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private FogNodeService fogNodeService;

    @PostMapping("/report")
    public ResponseEntity<?> receiveReport(@RequestBody ReportRequest request) {
        try {
            boolean success = fogNodeService.processReport(
                    request.getDeviceId(),
                    request.getTimestamp(),
                    request.getSeq(),
                    request.getCiphertext(),
                    request.getCiphertextSq(),
                    request.getSignature()
            );

            if (success) {
                return ResponseEntity.ok("Report received and verified");
            } else {
                return ResponseEntity.status(403).body("Report rejected");
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Invalid report: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("OK");
    }
}
