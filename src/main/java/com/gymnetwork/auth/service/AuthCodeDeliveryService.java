package com.gymnetwork.auth.service;

public interface AuthCodeDeliveryService {

    void sendPasswordResetToken(String email, String resetToken);

    void sendOtp(String phoneNumber, String otp);
}
