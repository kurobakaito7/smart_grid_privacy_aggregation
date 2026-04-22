package com.smartgrid.fog;

import com.smartgrid.crypto.SchnorrSigner;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class FogNodeAggregator {
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private AtomicReference<BigInteger> sumCiphertext;
    private AtomicReference<BigInteger> sumSqCiphertext;
    private Set<String> participantDevices;
    private SchnorrSigner fogSigner;
    private String aggregationId;

    public FogNodeAggregator(LocalDateTime windowStart) {
        this.windowStart = windowStart;
        this.windowEnd = LocalDateTime.now();
        this.sumCiphertext = new AtomicReference<>(BigInteger.ZERO);
        this.sumSqCiphertext = new AtomicReference<>(BigInteger.ZERO);
        this.participantDevices = ConcurrentHashMap.newKeySet();
        this.fogSigner = new SchnorrSigner();
        this.aggregationId = "AGG-" + windowStart.toString() + "-" + System.currentTimeMillis();
    }

    public void addReport(String deviceId, BigInteger ciphertext, BigInteger ciphertextSq, BigInteger n2) {
        participantDevices.add(deviceId);

        sumCiphertext.updateAndGet(current -> {
            if (current.equals(BigInteger.ZERO)) {
                return ciphertext;
            }
            return current.multiply(ciphertext).mod(n2);
        });

        sumSqCiphertext.updateAndGet(current -> {
            if (current.equals(BigInteger.ZERO)) {
                return ciphertextSq;
            }
            return current.multiply(ciphertextSq).mod(n2);
        });
    }

    public String signAggregationResult(BigInteger n2) {
        String message = String.format("%s|%s|%s|%s|%d|%s",
                aggregationId,
                windowStart.toString(),
                windowEnd.toString(),
                sumCiphertext.get().toString(16),
                participantDevices.size(),
                String.join(",", participantDevices));

        return fogSigner.sign(message);
    }

    public BigInteger getSumCiphertext() {
        return sumCiphertext.get();
    }

    public BigInteger getSumSqCiphertext() {
        return sumSqCiphertext.get();
    }

    public Set<String> getParticipantDevices() {
        return participantDevices;
    }

    public int getParticipantCount() {
        return participantDevices.size();
    }

    public void updateWindowEnd() {
        this.windowEnd = LocalDateTime.now();
    }
}
