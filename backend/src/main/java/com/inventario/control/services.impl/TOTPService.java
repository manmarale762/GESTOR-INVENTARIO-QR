package com.inventario.control.services.impl;

import com.inventario.control.services.ITOTPService;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

@Service
public class TOTPService implements ITOTPService {

    private static final int TIME_STEP = 30; // El QR caduca cada 30 segundos
    private static final int DIGITS = 6;

    @Override
    public String generateTOTP(String secretKey) {
        long timeWindow = System.currentTimeMillis() / 1000L / TIME_STEP;
        return calculateTOTP(secretKey, timeWindow);
    }

    @Override
    public boolean verifyTOTP(String secretKey, String code) {
        long currentTimeWindow = System.currentTimeMillis() / 1000L / TIME_STEP;
        // Revisamos la ventana actual y la anterior por si hay retraso en la red
        for (int i = -1; i <= 0; i++) {
            if (calculateTOTP(secretKey, currentTimeWindow + i).equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String calculateTOTP(String secretKey, long timeWindow) {
        try {
            byte[] decodedKey = new Base32().decode(secretKey);
            byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();

            SecretKeySpec signKey = new SecretKeySpec(decodedKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);

            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0xF;

            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, DIGITS);

            return String.format("%0" + DIGITS + "d", (int) truncatedHash);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error en criptografía TOTP", e);
        }
    }
}