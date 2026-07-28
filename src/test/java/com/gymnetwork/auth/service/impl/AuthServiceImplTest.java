package com.gymnetwork.auth.service.impl;

import com.gymnetwork.auth.config.AuthProperties;
import com.gymnetwork.auth.dto.request.ForgotPasswordRequest;
import com.gymnetwork.auth.dto.request.LoginRequest;
import com.gymnetwork.auth.dto.request.RefreshTokenRequest;
import com.gymnetwork.auth.dto.request.RegisterRequest;
import com.gymnetwork.auth.dto.request.SendOtpRequest;
import com.gymnetwork.auth.security.JwtTokenProvider;
import com.gymnetwork.auth.service.AuthCodeDeliveryService;
import com.gymnetwork.auth.service.AuthTokenStore;
import com.gymnetwork.owner.repository.GymOwnerProfileRepository;
import com.gymnetwork.shared.enums.Role;
import com.gymnetwork.shared.service.WalletInternalService;
import com.gymnetwork.user.entity.UserEntity;
import com.gymnetwork.user.repository.UserProfileRepository;
import com.gymnetwork.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private GymOwnerProfileRepository gymOwnerProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private AuthTokenStore authTokenStore;
    @Mock
    private WalletInternalService walletInternalService;
    @Mock
    private AuthCodeDeliveryService authCodeDeliveryService;

    private AuthProperties authProperties;
    private static final long CONFIGURED_REFRESH_EXPIRATION_MS = 12_345L;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authService = new AuthServiceImpl(
                userRepository,
                userProfileRepository,
                gymOwnerProfileRepository,
                passwordEncoder,
                tokenProvider,
                authTokenStore,
                walletInternalService,
                authProperties,
                authCodeDeliveryService
        );
    }

    @Test
    void sendOtpUsesDeterministicOtpOnlyWhenDevModeIsEnabled() {
        authProperties.setDevModeEnabled(true);
        SendOtpRequest request = new SendOtpRequest();
        request.setPhoneNumber("+15551234567");

        authService.sendOtp(request);

        verify(authTokenStore).set("OTP:+15551234567", "123456", 5, TimeUnit.MINUTES);
        verify(authCodeDeliveryService, never()).sendOtp(eq("+15551234567"), eq("123456"));
    }

    @Test
    void sendOtpUsesRandomProviderDispatchedOtpWhenDevModeIsDisabled() {
        authProperties.setDevModeEnabled(false);
        SendOtpRequest request = new SendOtpRequest();
        request.setPhoneNumber("+15551234567");
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);

        authService.sendOtp(request);

        verify(authTokenStore).set(eq("OTP:+15551234567"), otpCaptor.capture(), eq(5L), eq(TimeUnit.MINUTES));
        assertThat(otpCaptor.getValue()).matches("\\d{6}");
        assertThat(otpCaptor.getValue()).isNotEqualTo("123456");
        verify(authCodeDeliveryService).sendOtp("+15551234567", otpCaptor.getValue());
    }

    @Test
    void forgotPasswordDispatchesResetTokenThroughProviderWhenDevModeIsDisabled() {
        authProperties.setDevModeEnabled(false);
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("demo@example.com");
        UserEntity user = UserEntity.builder().email("demo@example.com").build();
        user.setId(UUID.randomUUID());
        when(userRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        authService.forgotPassword(request);

        verify(authCodeDeliveryService).sendPasswordResetToken(eq("demo@example.com"), tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isNotBlank();
        verify(authTokenStore).set(eq("RESET_TOKEN:" + tokenCaptor.getValue()), eq(user.getId().toString()), eq(15L), eq(TimeUnit.MINUTES));
    }

    @Test
    void registerStoresRefreshTokenUsingConfiguredExpiry() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest();
        request.setEmail("member@example.com");
        request.setPhoneNumber("+15555550100");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("Member");
        request.setRole(Role.ROLE_USER);

        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(CONFIGURED_REFRESH_EXPIRATION_MS);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity savedUser = invocation.getArgument(0);
            savedUser.setId(userId);
            return savedUser;
        });
        when(tokenProvider.generateAccessToken(userId, request.getEmail(), Role.ROLE_USER.name())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        authService.register(request);

        verifyRefreshTokenStoredWithConfiguredExpiry(userId, "refresh-token");
    }

    @Test
    void loginStoresRefreshTokenUsingConfiguredExpiry() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user(userId);
        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(CONFIGURED_REFRESH_EXPIRATION_MS);
        when(tokenProvider.generateAccessToken(userId, user.getEmail(), user.getRole().name())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        authService.login(request);

        verifyRefreshTokenStoredWithConfiguredExpiry(userId, "refresh-token");
    }

    // --- helpers -------------------------------------------------------

    private UserEntity user(UUID userId) {
        UserEntity user = UserEntity.builder()
                .email("member@example.com")
                .passwordHash("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        user.setId(userId);
        return user;
    }

    private void verifyRefreshTokenStoredWithConfiguredExpiry(UUID userId, String refreshToken) {
        verify(authTokenStore).set(
                eq("REFRESH_TOKEN:" + userId),
                eq(refreshToken),
                eq(CONFIGURED_REFRESH_EXPIRATION_MS),
                eq(TimeUnit.MILLISECONDS)
        );
    }
}