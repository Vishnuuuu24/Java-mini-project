package com.example.stmgt.controller;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class DjangoPasswordHasher {

    private static final String DJANGO_ALGORITHM = "pbkdf2_sha256";
    private static final String JAVA_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 870000;
    private static final int HASH_BYTES = 32;
    private static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        String salt = generateSalt(12);
        byte[] hash = derive(rawPassword, salt, ITERATIONS, HASH_BYTES);
        String encoded = Base64.getEncoder().withoutPadding().encodeToString(hash);
        return DJANGO_ALGORITHM + "$" + ITERATIONS + "$" + salt + "$" + encoded;
    }

    private byte[] derive(String rawPassword, String salt, int iterations, int lengthBytes) {
        PBEKeySpec keySpec = new PBEKeySpec(
            rawPassword.toCharArray(),
            salt.getBytes(StandardCharsets.UTF_8),
            iterations,
            lengthBytes * 8
        );

        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(JAVA_ALGORITHM);
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to hash password", exception);
        } finally {
            keySpec.clearPassword();
        }
    }

    private String generateSalt(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(SALT_CHARS.length());
            builder.append(SALT_CHARS.charAt(index));
        }
        return builder.toString();
    }
}
