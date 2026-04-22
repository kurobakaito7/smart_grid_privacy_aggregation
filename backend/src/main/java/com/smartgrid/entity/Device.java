package com.smartgrid.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
@Data
public class Device {
    @Id
    @Column(name = "device_id", length = 32)
    private String deviceId;

    @Column(name = "public_key", columnDefinition = "TEXT", nullable = false)
    private String publicKey;

    @Column(name = "status", length = 16)
    private String status = "active";

    @Column(name = "last_report_time")
    private LocalDateTime lastReportTime;

    @Column(name = "last_seq")
    private Integer lastSeq = 0;

    @Column(name = "register_time")
    private LocalDateTime registerTime;

    @PrePersist
    protected void onCreate() {
        if (registerTime == null) {
            registerTime = LocalDateTime.now();
        }
    }
}
