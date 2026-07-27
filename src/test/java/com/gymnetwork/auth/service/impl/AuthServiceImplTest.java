package com.gymnetwork.auth.service.impl;

import com.gymnetwork.auth.dto.request.LoginRequest;
import com.gymnetwork.auth.dto.request.RefreshTokenRequest;
import com.gymnetwork.auth.dto.request.RegisterRequest;
import com.gymnetwork.auth.security.JwtTokenProvider;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final long CONFIGURED_REFRESH_EXPIRATION_MS = 12_345L;

    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private GymOwnerProfileRepository gymOwnerProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private AuthTokenStore authTokenStore;
    @Mock private WalletInternalService walletInternalService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                userProfileRepository,
                gymOwnerProfileRepository,
                passwordEncoder,
                tokenProvider,
                authTokenStore,
                walletInternalService
        );
        when(tokenProvider.getRefreshTokenExpirationInMs()).thenReturn(CONFIGURED_REFRESH_EXPIRATION_MS);
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
        when(tokenProvider.generateAccessToken(userId, user.getEmail(), user.getRole().name())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        authService.login(request);

        verifyRefreshTokenStoredWithConfiguredExpiry(userId, "refresh-token");
    }

    @Test
    void refreshStoresRotatedRefreshTokenUsingConfiguredExpiry() {
        UUID userId = UUID.randomUUID();
        UserEntity user = user(userId);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("current-refresh-token");

        when(tokenProvider.validateToken(request.getRefreshToken())).thenReturn(true);
        when(tokenProvider.getUserIdFromJWT(request.getRefreshToken())).thenReturn(userId);
        when(authTokenStore.get("REFRESH_TOKEN:" + userId)).thenReturn(request.getRefreshToken());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(userId, user.getEmail(), user.getRole().name())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("new-refresh-token");

        authService.refreshToken(request);

        verifyRefreshTokenStoredWithConfiguredExpiry(userId, "new-refresh-token");
    }

    private void verifyRefreshTokenStoredWithConfiguredExpiry(UUID userId, String refreshToken) {
        verify(authTokenStore).set(
                eq("REFRESH_TOKEN:" + userId),
                eq(refreshToken),
                eq(CONFIGURED_REFRESH_EXPIRATION_MS),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    private UserEntity user(UUID userId) {
        UserEntity user = UserEntity.builder()
                .email("member@example.com")
                .phoneNumber("+15555550100")
                .passwordHash("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        user.setId(userId);
        return user;
    }
}
