package com.smartgrid.dto;

import lombok.Data;

@Data
public class DeviceStatus {
    private String deviceId;
    private String status;
    private String lastReportTime;
    private Integer lastSeq;
    private String publicKey;
}
