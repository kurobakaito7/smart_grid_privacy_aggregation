package com.smartgrid.meter;

import com.smartgrid.crypto.DataPacker;
import com.smartgrid.crypto.PaillierCrypto;
import com.smartgrid.crypto.SchnorrSigner;
import lombok.Data;

import java.math.BigInteger;
import java.util.Random;

@Data
public class MeterSimulator {
    private String deviceId;
    private SchnorrSigner signer;
    private PaillierCrypto paillier;
    private DataPacker dataPacker;
    private int[] maxValues;
    private int sequenceNumber;
    private Random random;

    public MeterSimulator(String deviceId, PaillierCrypto paillier) {
        this.deviceId = deviceId;
        this.paillier = paillier;
        this.signer = new SchnorrSigner();
        this.dataPacker = new DataPacker(DataPacker.generateDefaultMaxValues());
        this.maxValues = DataPacker.generateDefaultMaxValues();
        this.sequenceNumber = 0;
        this.random = new Random();
    }

    public int[] generateRandomData() {
        int voltage = random.nextInt(maxValues[0]) + 1;
        int current = random.nextInt(maxValues[1]) + 1;
        int activePower = random.nextInt(maxValues[2]) + 1;
        int reactivePower = random.nextInt(maxValues[3]) + 1;
        int powerFactor = random.nextInt(maxValues[4]) + 1;

        return new int[]{voltage, current, activePower, reactivePower, powerFactor};
    }

    public String[] encryptAndSign(int[] data, long timestamp) {
        BigInteger packedData = dataPacker.pack(data);
        BigInteger packedSq = packedData.multiply(packedData).mod(paillier.getN2());

        BigInteger ciphertext = paillier.encrypt(packedData);
        BigInteger ciphertextSq = paillier.encrypt(packedSq);

        String message = String.format("%s|%d|%d|%s|%s",
                deviceId, timestamp, sequenceNumber,
                ciphertext.toString(16), ciphertextSq.toString(16));

        String signature = signer.sign(message);

        sequenceNumber++;

        return new String[]{
                ciphertext.toString(16),
                ciphertextSq.toString(16),
                signature
        };
    }

    public String getPublicKey() {
        return signer.getPublicKeyHex();
    }

    public int getNextSequence() {
        return sequenceNumber;
    }
}
