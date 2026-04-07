package com.inventario.control.services;

public interface ITOTPService {
    String generateTOTP(String secretKey);
    boolean verifyTOTP(String secretKey, String code);
}