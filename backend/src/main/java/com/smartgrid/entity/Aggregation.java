package com.smartgrid.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregation")
@Data
public class Aggregation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agg_id")
    private Long aggId;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @Column(name = "participant_list", columnDefinition = "JSON")
    private String participantList;

    @Column(name = "sum_power", nullable = false, precision = 12, scale = 2)
    private BigDecimal sumPower;

    @Column(name = "sum_voltage", nullable = false, precision = 10, scale = 2)
    private BigDecimal sumVoltage;

    @Column(name = "sum_current", nullable = false, precision = 10, scale = 2)
    private BigDecimal sumCurrent;

    @Column(name = "fog_signature", length = 128, nullable = false)
    private String fogSignature;

    @Column(name = "verify_result", nullable = false)
    private Boolean verifyResult;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}
