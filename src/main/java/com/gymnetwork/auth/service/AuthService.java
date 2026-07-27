package com.gymnetwork.auth.service;

import com.gymnetwork.auth.dto.request.*;
import com.gymnetwork.auth.dto.response.AuthResponse;
import com.gymnetwork.auth.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(UUID userId);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void sendOtp(SendOtpRequest request);
    boolean verifyOtp(VerifyOtpRequest request);
    void changePassword(UUID userId, ChangePasswordRequest request);
    UserResponse getCurrentUser(UUID userId);
}
