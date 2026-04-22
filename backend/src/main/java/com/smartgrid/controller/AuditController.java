package com.smartgrid.controller;

import com.smartgrid.audit.AuditLogger;
import com.smartgrid.entity.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditLogger auditLogger;

    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<AuditLog> logs;

        if (level != null) {
            logs = auditLogger.getLogsByLevel(level);
        } else if (module != null) {
            logs = auditLogger.getLogsByModule(module);
        } else if (startTime != null && endTime != null) {
            logs = auditLogger.getLogs(startTime, endTime);
        } else {
            logs = auditLogger.getLogs(
                    LocalDateTime.now().minusDays(7),
                    LocalDateTime.now()
            );
        }

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<AuditLog> logs = auditLogger.getLogs(startTime, endTime);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Time,Level,Module,DeviceID,EventType,Message\n");

        for (AuditLog log : logs) {
            csv.append(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    log.getAuditId(),
                    log.getEventTime(),
                    log.getLevel(),
                    log.getModule(),
                    log.getDeviceId() != null ? log.getDeviceId() : "",
                    log.getEventType(),
                    log.getMessage().replace(",", ";")));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit_logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }
}
