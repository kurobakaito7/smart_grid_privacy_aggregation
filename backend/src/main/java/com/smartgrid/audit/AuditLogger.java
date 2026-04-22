package com.smartgrid.audit;

import com.smartgrid.entity.AuditLog;
import com.smartgrid.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogger {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async
    public void logSecurityEvent(String module, String deviceId, String eventType, String message) {
        logSecurityEvent(module, deviceId, eventType, message, null);
    }

    @Async
    public void logSecurityEvent(String module, String deviceId, String eventType, String message, String detail) {
        AuditLog log = new AuditLog();
        log.setEventTime(LocalDateTime.now());
        log.setLevel("WARN");
        log.setModule(module);
        log.setDeviceId(deviceId);
        log.setEventType(eventType);
        log.setMessage(message);
        log.setDetail(detail);

        auditLogRepository.save(log);

        System.out.println(String.format("[AUDIT] [%s] [%s] [%s] %s",
                log.getEventTime(), module, eventType, message));
    }

    @Async
    public void logInfo(String module, String eventType, String message) {
        AuditLog log = new AuditLog();
        log.setEventTime(LocalDateTime.now());
        log.setLevel("INFO");
        log.setModule(module);
        log.setEventType(eventType);
        log.setMessage(message);

        auditLogRepository.save(log);
    }

    @Async
    public void logError(String module, String deviceId, String eventType, String message, Exception e) {
        AuditLog log = new AuditLog();
        log.setEventTime(LocalDateTime.now());
        log.setLevel("ERROR");
        log.setModule(module);
        log.setDeviceId(deviceId);
        log.setEventType(eventType);
        log.setMessage(message);
        log.setDetail(e != null ? e.getMessage() : null);

        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogs(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByEventTimeBetween(startTime, endTime);
    }

    public List<AuditLog> getLogsByLevel(String level) {
        return auditLogRepository.findByLevel(level);
    }

    public List<AuditLog> getLogsByModule(String module) {
        return auditLogRepository.findByModule(module);
    }
}
