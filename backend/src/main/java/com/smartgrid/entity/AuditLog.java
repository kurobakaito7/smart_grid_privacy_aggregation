package com.smartgrid.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "level", length = 16, nullable = false)
    private String level;

    @Column(name = "module", length = 32, nullable = false)
    private String module;

    @Column(name = "device_id", length = 32)
    private String deviceId;

    @Column(name = "event_type", length = 64, nullable = false)
    private String eventType;

    @Column(name = "message", length = 512, nullable = false)
    private String message;

    @Column(name = "detail", columnDefinition = "JSON")
    private String detail;

    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
}
