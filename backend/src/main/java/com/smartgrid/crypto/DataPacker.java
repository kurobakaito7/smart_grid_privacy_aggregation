package com.smartgrid.crypto;

import java.math.BigInteger;

public class DataPacker {

    private BigInteger[] sequence;
    private int k;
    private BigInteger pMax;

    public DataPacker(int[] maxValues) {
        this.k = maxValues.length;
        this.sequence = generateSequence(maxValues);
        this.pMax = calculatePMax(maxValues, sequence);
    }

    private BigInteger[] generateSequence(int[] maxValues) {
        BigInteger[] a = new BigInteger[k];
        a[0] = BigInteger.ONE;

        for (int i = 1; i < k; i++) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = 0; j < i; j++) {
                sum = sum.add(a[j].multiply(BigInteger.valueOf(maxValues[j])));
            }
            a[i] = sum.add(BigInteger.ONE);
        }

        return a;
    }

    private BigInteger calculatePMax(int[] maxValues, BigInteger[] a) {
        BigInteger pMax = BigInteger.ZERO;
        for (int i = 0; i < k; i++) {
            pMax = pMax.add(a[i].multiply(BigInteger.valueOf(maxValues[i])));
        }
        return pMax.add(BigInteger.ONE);
    }

    public BigInteger pack(int[] data) {
        if (data.length != k) {
            throw new IllegalArgumentException("Data length must be " + k);
        }

        for (int i = 0; i < k; i++) {
            if (data[i] < 0 || data[i] > getMaxValues()[i]) {
                throw new IllegalArgumentException("Data[" + i + "] out of range: " + data[i]);
            }
        }

        BigInteger result = BigInteger.ZERO;
        for (int i = 0; i < k; i++) {
            result = result.add(sequence[i].multiply(BigInteger.valueOf(data[i])));
        }

        return result;
    }

    public int[] unpack(BigInteger packed) {
        int[] data = new int[k];
        BigInteger remaining = packed;

        for (int i = k - 1; i >= 0; i--) {
            BigInteger[] div = remaining.divideAndRemainder(sequence[i]);
            data[i] = div[0].intValue();
            remaining = div[1];
        }

        return data;
    }

    public BigInteger getPMax() {
        return pMax;
    }

    public int getK() {
        return k;
    }

    public BigInteger[] getSequence() {
        return sequence;
    }

    public int[] getMaxValues() {
        int[] maxValues = new int[k];
        for (int i = 0; i < k; i++) {
            maxValues[i] = sequence[i].subtract(BigInteger.ONE)
                    .divide(sequence[i > 0 ? i - 1 : 0]).intValue();
        }
        return maxValues;
    }

    public static int[] generateDefaultMaxValues() {
        return new int[]{250, 2000, 5000, 5000, 100};
    }

    public static DataPacker createDefault() {
        return new DataPacker(generateDefaultMaxValues());
    }
}
