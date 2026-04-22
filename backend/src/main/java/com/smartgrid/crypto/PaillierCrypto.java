package com.smartgrid.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.DHParameterSpec;

public class PaillierCrypto {

    private BigInteger n;
    private BigInteger n2;
    private BigInteger lambda;
    private BigInteger mu;
    private BigInteger g;
    private SecureRandom random;
    private int keyLength;

    static {
        if (java.security.Security.getProvider("BC") == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }

    public PaillierCrypto(int keyLength) {
        this.keyLength = keyLength;
        this.random = new SecureRandom();
        generateKey();
    }

    public PaillierCrypto(BigInteger n, BigInteger lambda, BigInteger mu) {
        this.n = n;
        this.n2 = n.multiply(n);
        this.lambda = lambda;
        this.mu = mu;
        this.g = n.add(BigInteger.ONE);
        this.random = new SecureRandom();
    }

    private void generateKey() {
        int pLength = keyLength / 2;
        int qLength = keyLength / 2;

        BigInteger p, q, n, lambda, x;

        do {
            p = BigInteger.probablePrime(pLength, random);
            q = BigInteger.probablePrime(qLength, random);
            n = p.multiply(q);
        } while (n.bitLength() != keyLength);

        lambda = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)).divide(
                p.subtract(BigInteger.ONE).gcd(q.subtract(BigInteger.ONE))
        );

        x = n.modInverse(lambda);
        mu = x;

        this.n = n;
        this.n2 = n.multiply(n);
        this.lambda = lambda;
        this.mu = mu;
        this.g = n.add(BigInteger.ONE);
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getN2() {
        return n2;
    }

    public BigInteger getLambda() {
        return lambda;
    }

    public BigInteger getMu() {
        return mu;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger encrypt(BigInteger m) {
        if (m.compareTo(BigInteger.ZERO) < 0 || m.compareTo(n) >= 0) {
            throw new IllegalArgumentException("Message must be in range [0, n)");
        }

        BigInteger gm = BigInteger.ONE.add(m.multiply(n)).mod(n2);
        BigInteger r = new BigInteger(keyLength, random);
        r = r.mod(n);

        BigInteger rn = r.modPow(n, n2);

        return gm.multiply(rn).mod(n2);
    }

    public BigInteger decrypt(BigInteger c) {
        BigInteger u = c.modPow(lambda, n2);
        BigInteger L = u.subtract(BigInteger.ONE).divide(n);
        return L.multiply(mu).mod(n);
    }

    public BigInteger add(BigInteger c1, BigInteger c2) {
        return c1.multiply(c2).mod(n2);
    }

    public BigInteger addScalar(BigInteger c, BigInteger m) {
        if (m.compareTo(BigInteger.ZERO) < 0 || m.compareTo(n) >= 0) {
            throw new IllegalArgumentException("Scalar must be in range [0, n)");
        }

        BigInteger gm = BigInteger.ONE.add(m.multiply(n)).mod(n2);
        return c.multiply(gm).mod(n2);
    }

    public String encryptToBase64(BigInteger m) {
        return encrypt(m).toString(16);
    }

    public BigInteger decryptFromBase64(String ciphertext) {
        return decrypt(new BigInteger(ciphertext, 16));
    }
}
