package com.smartgrid.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AggregationRequest {
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private int participantCount;
    private String participantList;
    private String sumCiphertext;
    private String sumSqCiphertext;
    private String fogSignature;
}
