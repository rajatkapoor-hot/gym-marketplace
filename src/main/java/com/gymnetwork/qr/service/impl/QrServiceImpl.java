package com.gymnetwork.qr.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gymnetwork.qr.dto.response.QrResponse;
import com.gymnetwork.qr.service.QrService;
import com.gymnetwork.shared.dto.QrPayload;
import com.gymnetwork.shared.service.GymInternalService;
import com.gymnetwork.shared.service.QrInternalService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrServiceImpl implements QrService, QrInternalService {

    private final GymInternalService gymInternalService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    /**
     * No default value on purpose: an unset key must fail application startup
     * rather than silently fall back to a key value that's sitting in source control.
     * Must be a hex-encoded string representing 16, 24, or 32 raw bytes
     * (AES-128 / AES-192 / AES-256).
     */
    @Value("${app.qr.encryption-key}")
    private String secretKeyHex;

    @Value("${app.qr.ttl:PT24H}")
    private Duration qrTtl;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int PAYLOAD_VERSION = 1;
    private static final int QR_CODE_SIZE = 300;
    private static final String PNG_FORMAT = "PNG";
    private static final Set<Integer> VALID_KEY_LENGTHS = Set.of(16, 24, 32);

    private byte[] keyBytes;

    @PostConstruct
    void validateKey() {
        byte[] decoded = decodeHexKey(secretKeyHex);
        if (!VALID_KEY_LENGTHS.contains(decoded.length)) {
            throw new IllegalStateException(
                    "app.qr.encryption-key must decode to 16, 24, or 32 bytes (128/192/256-bit AES key), "
                            + "but decoded to " + decoded.length + " bytes");
        }
        this.keyBytes = decoded;
    }

    @Override
    public QrResponse getGymQr(UUID gymId) {
        if (!gymInternalService.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym not found with ID: " + gymId);
        }
        String encryptedPayload = encryptPayload(newPayload(gymId));
        return QrResponse.builder()
                .gymId(gymId)
                .encryptedPayload(encryptedPayload)
                .qrCodeDataUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(generateQrPng(encryptedPayload)))
                .build();
    }

    @Override
    public QrResponse regenerateGymQr(UUID gymId) {
        return getGymQr(gymId);
    }

    @Override
    public byte[] generateQrImage(UUID gymId) {
        if (!gymInternalService.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym not found with ID: " + gymId);
        }
        // Encrypt a proper QrPayload (not just the raw UUID) so this stays
        // decryptable by decryptPayload, which always expects QrPayload JSON.
        return generateQrPng(encryptPayload(newPayload(gymId)));
    }

    @Override
    public QrPayload decryptPayload(String encryptedPayload) {
        byte[] cipherTextWithIv = decodeBase64(encryptedPayload);
        if (cipherTextWithIv.length <= IV_LENGTH) {
            throw new BadRequestException("Malformed QR code payload");
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(cipherTextWithIv, 0, iv, 0, IV_LENGTH);

            int cipherTextSize = cipherTextWithIv.length - IV_LENGTH;
            byte[] cipherText = new byte[cipherTextSize];
            System.arraycopy(cipherTextWithIv, IV_LENGTH, cipherText, 0, cipherTextSize);

            byte[] plainText = decrypt(cipherText, iv);
            QrPayload payload = readPayload(plainText);
            validatePayload(payload);
            return payload;
        } catch (AEADBadTagException e) {
            log.warn("QR payload failed authentication tag validation");
            throw new BadRequestException("Invalid or tampered QR code payload");
        } catch (GeneralSecurityException e) {
            log.error("Failed to decrypt QR payload", e);
            throw new BadRequestException("Unable to decrypt QR code payload");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new BadRequestException("Unable to read QR code payload");
        }
    }

    private QrPayload newPayload(UUID gymId) {
        Instant issuedAt = Instant.now(clock);
        return new QrPayload(gymId, issuedAt, issuedAt.plus(qrTtl), PAYLOAD_VERSION);
    }

    private byte[] decodeBase64(String encryptedPayload) {
        if (encryptedPayload == null || encryptedPayload.isBlank()) {
            throw new BadRequestException("Malformed QR code payload");
        }
        try {
            return Base64.getDecoder().decode(encryptedPayload);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Malformed QR code payload");
        }
    }

    private QrPayload readPayload(byte[] plainText) throws IOException {
        try {
            return objectMapper.readValue(plainText, QrPayload.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("QR code payload is not valid JSON");
        }
    }

    private void validatePayload(QrPayload payload) {
        if (payload.gymId() == null || payload.issuedAt() == null || payload.expiresAt() == null || payload.version() <= 0) {
            throw new BadRequestException("QR code payload is missing required fields");
        }
        if (payload.version() != PAYLOAD_VERSION) {
            throw new BadRequestException("Unsupported QR code payload version: " + payload.version());
        }
        Instant now = Instant.now(clock);
        if (!payload.expiresAt().isAfter(now)) {
            throw new BadRequestException("QR code has expired");
        }
    }

    private String encryptPayload(QrPayload payload) {
        try {
            return encrypt(objectMapper.writeValueAsBytes(payload));
        } catch (JsonProcessingException e) {
            log.error("Error serializing QR payload", e);
            throw new RuntimeException("Failed to encrypt QR payload", e);
        }
    }

    /**
     * Shared AES-GCM encrypt routine: generates a fresh random IV, encrypts
     * plaintext, and returns Base64(iv || ciphertext+tag).
     */
    private String encrypt(byte[] plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherText = cipher.doFinal(plainText);
            byte[] finalPayload = new byte[IV_LENGTH + cipherText.length];

            System.arraycopy(iv, 0, finalPayload, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, finalPayload, IV_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(finalPayload);
        } catch (GeneralSecurityException e) {
            log.error("Error encrypting QR payload", e);
            throw new RuntimeException("Failed to encrypt QR payload", e);
        }
    }

    private byte[] decrypt(byte[] cipherText, byte[] iv) throws GeneralSecurityException {
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        return cipher.doFinal(cipherText);
    }

    private byte[] generateQrPng(String encryptedPayload) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(encryptedPayload, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, PNG_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Error generating QR PNG image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    private byte[] decodeHexKey(String hex) {
        if (hex == null || hex.isBlank()) {
            throw new IllegalStateException("app.qr.encryption-key must be configured");
        }
        String normalized = hex.trim();
        if (normalized.length() % 2 != 0) {
            throw new IllegalStateException("app.qr.encryption-key must be a valid hex string (even length)");
        }
        byte[] result = new byte[normalized.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int hi = Character.digit(normalized.charAt(i * 2), 16);
            int lo = Character.digit(normalized.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalStateException("app.qr.encryption-key must be a valid hex string");
            }
            result[i] = (byte) ((hi << 4) + lo);
        }
        return result;
    }
}