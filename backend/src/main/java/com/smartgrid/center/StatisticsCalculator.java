package com.smartgrid.center;

import com.smartgrid.crypto.DataPacker;
import com.smartgrid.crypto.PaillierCrypto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Service
public class StatisticsCalculator {

    private final PaillierCrypto paillier;
    private final DataPacker dataPacker;

    public StatisticsCalculator(@Value("${smartgrid.paillier.key-length}") int keyLength) {
        this.paillier = new PaillierCrypto(keyLength);
        this.dataPacker = new DataPacker(DataPacker.generateDefaultMaxValues());
    }

    public int[] decryptAndUnpack(BigInteger ciphertext) {
        BigInteger packed = paillier.decrypt(ciphertext);
        return dataPacker.unpack(packed);
    }

    public BigDecimal[] calculateStatistics(int[] sumValues, int[] sumSqValues, int count) {
        if (count == 0) {
            return new BigDecimal[0];
        }

        BigDecimal[] stats = new BigDecimal[5];

        for (int i = 0; i < 5; i++) {
            BigDecimal sum = BigDecimal.valueOf(sumValues[i]);
            BigDecimal sumSq = BigDecimal.valueOf(sumSqValues[i]);

            BigDecimal mean = sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            BigDecimal meanSq = sumSq.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            BigDecimal variance = meanSq.subtract(mean.multiply(mean));

            if (i == 0) {
                stats[0] = mean;
                stats[5] = variance;
            } else if (i == 1) {
                stats[1] = mean;
                stats[6] = variance;
            } else if (i == 2) {
                stats[2] = mean;
                stats[7] = variance;
            } else if (i == 3) {
                stats[3] = mean;
                stats[8] = variance;
            } else if (i == 4) {
                stats[4] = mean;
                stats[9] = variance;
            }
        }

        return stats;
    }

    public BigDecimal[] computeMeanAndVariance(int[] sumValues, int[] sumSqValues, int count) {
        if (count <= 0) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        }

        BigDecimal[] result = new BigDecimal[6];

        for (int i = 0; i < 5; i++) {
            BigDecimal sum = BigDecimal.valueOf(sumValues[i]);
            BigDecimal sumSq = BigDecimal.valueOf(sumSqValues[i]);

            BigDecimal mean = sum.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            BigDecimal meanSq = sumSq.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
            BigDecimal variance = meanSq.subtract(mean.multiply(mean)).max(BigDecimal.ZERO);

            result[i] = mean;
            result[5 + i] = variance;
        }

        return result;
    }

    public PaillierCrypto getPaillier() {
        return paillier;
    }
}
