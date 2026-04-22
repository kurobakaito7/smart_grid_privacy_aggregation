package com.smartgrid.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_log")
@Data
public class ReportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "device_id", length = 32, nullable = false)
    private String deviceId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "seq", nullable = false)
    private Integer seq;

    @Column(name = "ciphertext", columnDefinition = "TEXT", nullable = false)
    private String ciphertext;

    @Column(name = "ciphertext_sq", columnDefinition = "TEXT", nullable = false)
    private String ciphertextSq;

    @Column(name = "signature", length = 128, nullable = false)
    private String signature;

    @Column(name = "verify_result", nullable = false)
    private Boolean verifyResult;

    @Column(name = "reject_reason", length = 256)
    private String rejectReason;
}
