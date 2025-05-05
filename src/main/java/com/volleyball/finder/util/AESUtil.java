package com.volleyball.finder.util;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * AES‑256‑GCM utility.
 * <p>Thread‑safe; one {@link SecureRandom} instance shared by the VM.</p>
 */
public final class AESUtil {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LEN = 32;   // 256‑bit
    private static final int IV_LEN = 12;   // 96‑bit (NIST‑recommended)
    private static final int TAG_BITS = 128;  // Auth tag length

    private static final SecureRandom RNG = new SecureRandom();

    private final SecretKey key;

    /**
     * Pass raw 32‑byte key material (keep it in env vars / KMS, not in source).
     */
    public AESUtil(byte[] rawKey) {
        Objects.requireNonNull(rawKey, "rawKey");
        if (rawKey.length != KEY_LEN) {
            throw new IllegalArgumentException("Key must be 256‑bit (" + KEY_LEN + " bytes).");
        }
        this.key = new SecretKeySpec(rawKey, "AES");
    }

    public String encrypt(String plainText) {
        byte[] iv = new byte[IV_LEN];
        RNG.nextBytes(iv);

        byte[] cipherAndTag = crypt(Cipher.ENCRYPT_MODE, iv, plainText.getBytes(StandardCharsets.UTF_8));
        byte[] payload = ByteBuffer.allocate(iv.length + cipherAndTag.length)
                .put(iv)
                .put(cipherAndTag)
                .array();
        return Base64.getEncoder().encodeToString(payload);
    }

    public String decrypt(String base64Payload) {
        byte[] payload = Base64.getDecoder().decode(base64Payload);

        byte[] iv = Arrays.copyOfRange(payload, 0, IV_LEN);
        byte[] cipherAndTag = Arrays.copyOfRange(payload, IV_LEN, payload.length);

        byte[] plain = crypt(Cipher.DECRYPT_MODE, iv, cipherAndTag);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private byte[] crypt(int mode, byte[] iv, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES‑GCM operation failed", e);
        }
    }
}