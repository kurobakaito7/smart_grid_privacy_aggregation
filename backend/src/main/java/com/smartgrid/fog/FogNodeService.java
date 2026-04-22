package com.smartgrid.fog;

import com.smartgrid.audit.AuditLogger;
import com.smartgrid.crypto.SchnorrSigner;
import com.smartgrid.entity.Device;
import com.smartgrid.entity.ReportLog;
import com.smartgrid.repository.DeviceRepository;
import com.smartgrid.repository.ReportLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FogNodeService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReportLogRepository reportLogRepository;

    @Autowired
    private AuditLogger auditLogger;

    @Value("${smartgrid.replay.time-window-ms}")
    private long replayTimeWindowMs;

    @Value("${smartgrid.paillier.key-length:1024}")
    private int paillierKeyLength;

    private final Map<String, Integer> lastSeqMap = new ConcurrentHashMap<>();
    private final List<FogNodeAggregator> aggregators = new ArrayList<>();
    private FogNodeAggregator currentAggregator;
    private BigInteger n;
    private BigInteger n2;

    @PostConstruct
    public void init() {
        initializePaillierParams();
    }

    private void initializePaillierParams() {
        com.smartgrid.crypto.PaillierCrypto tempPaillier = new com.smartgrid.crypto.PaillierCrypto(paillierKeyLength);
        this.n = tempPaillier.getN();
        this.n2 = tempPaillier.getN2();
    }

    @Scheduled(fixedDelayString = "${smartgrid.aggregate.window}")
    public void startNewAggregationWindow() {
        if (currentAggregator != null && currentAggregator.getParticipantCount() > 0) {
            currentAggregator.updateWindowEnd();
            aggregators.add(currentAggregator);
        }
        currentAggregator = new FogNodeAggregator(LocalDateTime.now());
    }

    public boolean processReport(String deviceId, long timestamp, int seq,
                                 String ciphertextHex, String ciphertextSqHex, String signatureHex) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            auditLogger.logSecurityEvent("FOG_NODE", deviceId, "UNKNOWN_DEVICE",
                    "Report from unknown device: " + deviceId);
            return false;
        }

        if (!checkReplay(deviceId, seq, timestamp)) {
            saveReportLog(deviceId, timestamp, seq, ciphertextHex, ciphertextSqHex, signatureHex, false, "REPLAY_ATTACK");
            return false;
        }

        String publicKey = device.getPublicKey();
        String message = String.format("%s|%d|%d|%s|%s", deviceId, timestamp, seq, ciphertextHex, ciphertextSqHex);

        SchnorrSigner verifier = new SchnorrSigner();
        if (!verifier.verify(message, signatureHex, publicKey)) {
            auditLogger.logSecurityEvent("FOG_NODE", deviceId, "SIGNATURE_VERIFY_FAILED",
                    "Signature verification failed for device: " + deviceId);
            saveReportLog(deviceId, timestamp, seq, ciphertextHex, ciphertextSqHex, signatureHex, false, "SIGNATURE_INVALID");
            return false;
        }

        BigInteger ciphertext = new BigInteger(ciphertextHex, 16);
        BigInteger ciphertextSq = new BigInteger(ciphertextSqHex, 16);

        if (currentAggregator == null) {
            currentAggregator = new FogNodeAggregator(LocalDateTime.now());
        }

        currentAggregator.addReport(deviceId, ciphertext, ciphertextSq, n2);

        device.setLastReportTime(LocalDateTime.now());
        device.setLastSeq(seq);
        deviceRepository.save(device);

        saveReportLog(deviceId, timestamp, seq, ciphertextHex, ciphertextSqHex, signatureHex, true, null);

        return true;
    }

    private boolean checkReplay(String deviceId, int seq, long timestamp) {
        Integer lastSeq = lastSeqMap.get(deviceId);

        if (lastSeq != null && seq <= lastSeq) {
            auditLogger.logSecurityEvent("FOG_NODE", deviceId, "REPLAY_ATTACK",
                    "Replay attack detected: current seq=" + seq + ", last seq=" + lastSeq);
            return false;
        }

        long now = System.currentTimeMillis();
        if (Math.abs(timestamp - now) > replayTimeWindowMs) {
            auditLogger.logSecurityEvent("FOG_NODE", deviceId, "TIMESTAMP_OUT_OF_WINDOW",
                    "Timestamp out of window: diff=" + (timestamp - now) + "ms");
            return false;
        }

        lastSeqMap.put(deviceId, seq);
        return true;
    }

    private void saveReportLog(String deviceId, long timestamp, int seq,
                               String ciphertext, String ciphertextSq, String signature,
                               boolean verifyResult, String rejectReason) {
        ReportLog log = new ReportLog();
        log.setDeviceId(deviceId);
        log.setTimestamp(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                java.time.ZoneId.systemDefault()));
        log.setSeq(seq);
        log.setCiphertext(ciphertext);
        log.setCiphertextSq(ciphertextSq);
        log.setSignature(signature);
        log.setVerifyResult(verifyResult);
        log.setRejectReason(rejectReason);
        reportLogRepository.save(log);
    }

    public FogNodeAggregator getCurrentAggregator() {
        return currentAggregator;
    }

    public List<FogNodeAggregator> getAggregators() {
        return aggregators;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getN2() {
        return n2;
    }
}
