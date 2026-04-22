package com.smartgrid.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private int participantCount;
    private List<String> participantList;
    private BigDecimal avgPower;
    private BigDecimal avgVoltage;
    private BigDecimal avgCurrent;
    private BigDecimal varPower;
    private BigDecimal varVoltage;
    private BigDecimal varCurrent;
    private boolean signatureVerified;
}
