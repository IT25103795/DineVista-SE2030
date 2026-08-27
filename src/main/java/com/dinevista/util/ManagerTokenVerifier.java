package com.dinevista.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class ManagerTokenVerifier {
    // Only the SHA-256 fingerprint is stored. Set DINEVISTA_MANAGER_TOKEN_HASH to rotate it.
    private static final String DEFAULT_EXPECTED_HASH =
            "6b133f57c58ba05505f92f100d5017d9caf04be372177d7df6ac3bfea4802997";

    private ManagerTokenVerifier() {}

    public static boolean isValid(String suppliedToken) {
        if (suppliedToken == null || suppliedToken.isBlank()) return false;
        String expectedHex = System.getenv("DINEVISTA_MANAGER_TOKEN_HASH");
        if (expectedHex == null || !expectedHex.matches("(?i)[0-9a-f]{64}")) {
            expectedHex = DEFAULT_EXPECTED_HASH;
        }
        byte[] expected = decodeHex(expectedHex);
        byte[] actual = sha256(normalize(suppliedToken).getBytes(StandardCharsets.UTF_8));
        return MessageDigest.isEqual(expected, actual);
    }

    private static String normalize(String token) {
        return token.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private static byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private static byte[] decodeHex(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }
}
