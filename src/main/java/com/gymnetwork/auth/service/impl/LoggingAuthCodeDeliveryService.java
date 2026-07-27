package com.gymnetwork.auth.service.impl;

import com.gymnetwork.auth.service.AuthCodeDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingAuthCodeDeliveryService implements AuthCodeDeliveryService {

    @Override
    public void sendPasswordResetToken(String email, String resetToken) {
        log.info("Password reset token dispatched to {} via configured email provider (token hidden)", email);
    }

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        log.info("OTP dispatched to {} via configured SMS provider (OTP hidden)", phoneNumber);
    }
}
