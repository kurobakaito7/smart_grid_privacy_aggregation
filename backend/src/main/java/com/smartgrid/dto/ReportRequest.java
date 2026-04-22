package com.smartgrid.dto;

import lombok.Data;

@Data
public class ReportRequest {
    private String deviceId;
    private long timestamp;
    private int seq;
    private String ciphertext;
    private String ciphertextSq;
    private String signature;
}
