package com.example.stmgt.dto.validation;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Component
public class DjangoPasswordVerifier {

    private static final String DJANGO_PBKDF2_SHA256 = "pbkdf2_sha256";
    private static final String JAVA_PBKDF2_SHA256 = "PBKDF2WithHmacSHA256";

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }

        String[] parts = encodedPassword.split("\\$", 4);
        if (parts.length != 4 || !DJANGO_PBKDF2_SHA256.equals(parts[0])) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            return false;
        }

        if (iterations <= 0) {
            return false;
        }

        byte[] storedHash = decodeBase64(parts[3]);
        if (storedHash == null || storedHash.length == 0) {
            return false;
        }

        byte[] candidateHash = derivePbkdf2Sha256(rawPassword, parts[2], iterations, storedHash.length);
        if (candidateHash == null) {
            return false;
        }

        return constantTimeEquals(storedHash, candidateHash);
    }

    private byte[] derivePbkdf2Sha256(String rawPassword, String salt, int iterations, int derivedKeyLengthBytes) {
        PBEKeySpec keySpec = new PBEKeySpec(
            rawPassword.toCharArray(),
            salt.getBytes(StandardCharsets.UTF_8),
            iterations,
            derivedKeyLengthBytes * 8
        );

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(JAVA_PBKDF2_SHA256);
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException ex) {
            return null;
        } finally {
            keySpec.clearPassword();
        }
    }

    private byte[] decodeBase64(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        int remainder = normalized.length() % 4;
        if (remainder != 0) {
            normalized = normalized + "=".repeat(4 - remainder);
        }

        try {
            return Base64.getDecoder().decode(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean constantTimeEquals(byte[] left, byte[] right) {
        if (left == null || right == null) {
            return false;
        }

        int maxLength = Math.max(left.length, right.length);
        int diff = left.length ^ right.length;

        for (int i = 0; i < maxLength; i++) {
            byte leftByte = i < left.length ? left[i] : 0;
            byte rightByte = i < right.length ? right[i] : 0;
            diff |= leftByte ^ rightByte;
        }

        return diff == 0;
    }
}
