package com.gymnetwork.auth.service.impl;

import com.gymnetwork.auth.dto.request.*;
import com.gymnetwork.auth.dto.response.AuthResponse;
import com.gymnetwork.auth.dto.response.UserResponse;
import com.gymnetwork.auth.security.JwtTokenProvider;
import com.gymnetwork.auth.service.AuthService;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ConflictException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.common.exception.UnauthorizedException;
import com.gymnetwork.owner.entity.GymOwnerProfileEntity;
import com.gymnetwork.owner.repository.GymOwnerProfileRepository;
import com.gymnetwork.shared.enums.Role;
import com.gymnetwork.shared.service.WalletInternalService;
import com.gymnetwork.user.entity.UserEntity;
import com.gymnetwork.user.entity.UserProfileEntity;
import com.gymnetwork.user.repository.UserProfileRepository;
import com.gymnetwork.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.gymnetwork.auth.service.AuthTokenStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final GymOwnerProfileRepository gymOwnerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthTokenStore authTokenStore;
    private final WalletInternalService walletInternalService;

    private static final String REDIS_REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN:";
    private static final String REDIS_OTP_PREFIX = "OTP:";

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone number is already registered");
        }

        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status("ACTIVE")
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        UserEntity savedUser = userRepository.save(user);

        UserProfileEntity userProfile = UserProfileEntity.builder()
                .userId(savedUser.getId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userProfileRepository.save(userProfile);

        if (request.getRole() == Role.ROLE_GYM_OWNER) {
            GymOwnerProfileEntity ownerProfile = GymOwnerProfileEntity.builder()
                    .userId(savedUser.getId())
                    .businessName(request.getBusinessName() != null ? request.getBusinessName() : request.getFirstName() + " Business")
                    .verificationStatus("PENDING")
                    .build();
            gymOwnerProfileRepository.save(ownerProfile);
        }

        // Initialize User Wallet
        walletInternalService.createWalletForUser(savedUser.getId());

        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        authTokenStore.set(
                REDIS_REFRESH_TOKEN_PREFIX + savedUser.getId(),
                refreshToken,
                tokenProvider.getRefreshTokenExpirationInMs(), TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        authTokenStore.set(
                REDIS_REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                tokenProvider.getRefreshTokenExpirationInMs(), TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UUID userId = tokenProvider.getUserIdFromJWT(request.getRefreshToken());
        Object storedToken = authTokenStore.get(REDIS_REFRESH_TOKEN_PREFIX + userId);

        if (storedToken == null || !storedToken.toString().equals(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token is revoked or mismatched");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        authTokenStore.set(
                REDIS_REFRESH_TOKEN_PREFIX + user.getId(),
                newRefreshToken,
                tokenProvider.getRefreshTokenExpirationInMs(), TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void logout(UUID userId) {
        authTokenStore.delete(REDIS_REFRESH_TOKEN_PREFIX + userId);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    String resetToken = UUID.randomUUID().toString();
                    authTokenStore.set("RESET_TOKEN:" + resetToken, user.getId().toString(), 15, TimeUnit.MINUTES);
                    log.info("Simulated sending password reset email to: {} with token: {}", user.getEmail(), resetToken);
                });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Object userIdObj = authTokenStore.get("RESET_TOKEN:" + request.getToken());
        if (userIdObj == null) {
            throw new BadRequestException("Invalid or expired password reset token");
        }

        UUID userId = UUID.fromString(userIdObj.toString());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        authTokenStore.delete("RESET_TOKEN:" + request.getToken());
    }

    @Override
    public void sendOtp(SendOtpRequest request) {
        String mockOtp = "123456"; // Default mock OTP for development
        authTokenStore.set(REDIS_OTP_PREFIX + request.getPhoneNumber(), mockOtp, 5, TimeUnit.MINUTES);
        log.info("OTP sent to {}: {}", request.getPhoneNumber(), mockOtp);
    }

    @Override
    public boolean verifyOtp(VerifyOtpRequest request) {
        Object storedOtp = authTokenStore.get(REDIS_OTP_PREFIX + request.getPhoneNumber());
        if (storedOtp != null && storedOtp.toString().equals(request.getOtp())) {
            authTokenStore.delete(REDIS_OTP_PREFIX + request.getPhoneNumber());
            userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(user -> {
                user.setPhoneVerified(true);
                userRepository.save(user);
            });
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Incorrect current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .status(user.getStatus())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .build();
    }
}
