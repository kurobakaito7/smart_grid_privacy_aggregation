package com.smartgrid.center;

import com.smartgrid.crypto.SchnorrSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FogSignatureVerifier {

    @Value("${smartgrid.fog.public-key}")
    private String fogPublicKeyHex;

    private final SchnorrSigner verifier;

    public FogSignatureVerifier() {
        this.verifier = new SchnorrSigner();
    }

    public boolean verifyAggregationSignature(String aggregationId, String windowStart, String windowEnd,
                                               String sumCiphertext, int participantCount, 
                                               String participantList, String signatureHex) {
        String message = buildSignatureMessage(aggregationId, windowStart, windowEnd, 
                sumCiphertext, participantCount, participantList);
        return verifier.verify(message, signatureHex, fogPublicKeyHex);
    }

    public boolean verifyAggregationSignature(String windowStart, String windowEnd,
                                               String sumCiphertext, int participantCount, 
                                               String participantList, String signatureHex) {
        String aggregationId = "AGG-" + windowStart + "-" + windowEnd;
        return verifyAggregationSignature(aggregationId, windowStart, windowEnd, 
                sumCiphertext, participantCount, participantList, signatureHex);
    }

    private String buildSignatureMessage(String aggregationId, String windowStart, String windowEnd,
                                          String sumCiphertext, int participantCount, String participantList) {
        return String.format("%s|%s|%s|%s|%d|%s",
                aggregationId,
                windowStart,
                windowEnd,
                sumCiphertext,
                participantCount,
                participantList);
    }

    public void setFogPublicKeyHex(String fogPublicKeyHex) {
        this.fogPublicKeyHex = fogPublicKeyHex;
    }

    public String getFogPublicKeyHex() {
        return fogPublicKeyHex;
    }
}