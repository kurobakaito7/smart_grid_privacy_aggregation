package com.smartgrid.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SchnorrSigner {

    private static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(
            curve.getCurve(), curve.getG(), curve.getN(), curve.getH()
    );

    private final SecureRandom random;
    private BigInteger privateKey;
    private BigInteger publicKeyX;
    private BigInteger publicKeyY;

    static {
        if (java.security.Security.getProvider("BC") == null) {
            java.security.Security.addProvider(new BouncyCastleProvider());
        }
    }

    public SchnorrSigner() {
        this.random = new SecureRandom();
        generateKeyPair();
    }

    public SchnorrSigner(BigInteger privateKey) {
        this.random = new SecureRandom();
        this.privateKey = privateKey;

        org.bouncycastle.math.ec.ECPoint point = DOMAIN_PARAMS.getG().multiply(privateKey);
        this.publicKeyX = point.getAffineXCoord().toBigInteger();
        this.publicKeyY = point.getAffineYCoord().toBigInteger();
    }

    private void generateKeyPair() {
        privateKey = new BigInteger(DOMAIN_PARAMS.getN().bitLength(), random);
        privateKey = privateKey.mod(DOMAIN_PARAMS.getN());

        org.bouncycastle.math.ec.ECPoint point = DOMAIN_PARAMS.getG().multiply(privateKey);
        publicKeyX = point.getAffineXCoord().toBigInteger();
        publicKeyY = point.getAffineYCoord().toBigInteger();
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyHex() {
        String xHex = String.format("%064x", publicKeyX);
        String yHex = String.format("%064x", publicKeyY);
        return xHex + yHex;
    }

    public String sign(String message) {
        BigInteger hash = hashMessage(message);

        BigInteger k;
        do {
            k = new BigInteger(DOMAIN_PARAMS.getN().bitLength(), random);
            k = k.mod(DOMAIN_PARAMS.getN());
        } while (k.compareTo(BigInteger.ZERO) == 0);

        org.bouncycastle.math.ec.ECPoint R = DOMAIN_PARAMS.getG().multiply(k);
        BigInteger r = R.getAffineXCoord().toBigInteger().mod(DOMAIN_PARAMS.getN());

        BigInteger s = k.modInverse(DOMAIN_PARAMS.getN())
                .multiply(hash.add(privateKey.multiply(r)))
                .mod(DOMAIN_PARAMS.getN());

        String rHex = String.format("%064x", r);
        String sHex = String.format("%064x", s);

        return rHex + sHex;
    }

    public boolean verify(String message, String signatureHex, String publicKeyHex) {
        try {
            if (signatureHex.length() != 128) {
                return false;
            }

            BigInteger r = new BigInteger(signatureHex.substring(0, 64), 16);
            BigInteger s = new BigInteger(signatureHex.substring(64, 128), 16);

            if (r.compareTo(BigInteger.ONE) < 0 || r.compareTo(DOMAIN_PARAMS.getN()) >= 0) {
                return false;
            }
            if (s.compareTo(BigInteger.ONE) < 0 || s.compareTo(DOMAIN_PARAMS.getN()) >= 0) {
                return false;
            }

            String pubKeyXHex = publicKeyHex.substring(0, 64);
            String pubKeyYHex = publicKeyHex.substring(64, 128);
            BigInteger x = new BigInteger(pubKeyXHex, 16);
            BigInteger y = new BigInteger(pubKeyYHex, 16);

            org.bouncycastle.math.ec.ECPoint publicKey = DOMAIN_PARAMS.getCurve().createPoint(x, y);

            BigInteger hash = hashMessage(message);

            BigInteger w = s.modInverse(DOMAIN_PARAMS.getN());
            BigInteger u1 = hash.multiply(w).mod(DOMAIN_PARAMS.getN());
            BigInteger u2 = r.multiply(w).mod(DOMAIN_PARAMS.getN());

            org.bouncycastle.math.ec.ECPoint point = DOMAIN_PARAMS.getG().multiply(u1)
                    .add(publicKey.multiply(u2));

            BigInteger v = point.getAffineXCoord().toBigInteger().mod(DOMAIN_PARAMS.getN());

            return v.equals(r);
        } catch (Exception e) {
            return false;
        }
    }

    private BigInteger hashMessage(String message) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(message.getBytes("UTF-8"));
            return new BigInteger(1, hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash message", e);
        }
    }

    public static String extractPublicKeyFromHex(String publicKeyHex) {
        if (publicKeyHex.length() != 128) {
            throw new IllegalArgumentException("Invalid public key format");
        }
        return publicKeyHex;
    }
}
