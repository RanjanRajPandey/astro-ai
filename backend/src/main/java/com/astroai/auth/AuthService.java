package com.astroai.auth;

import com.astroai.auth.dto.AuthResponseDto;
import com.astroai.auth.dto.LoginRequestDto;
import com.astroai.auth.dto.RefreshTokenRequestDto;
import com.astroai.auth.dto.RegisterRequestDto;
import com.astroai.auth.dto.UserProfileResponseDto;
import com.astroai.auth.dto.UserSummaryDto;
import com.astroai.common.ResourceNotFoundException;
import com.astroai.user.User;
import com.astroai.user.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists: " + normalizedEmail);
        }

        String role = request.role() != null && !request.role().isBlank()
                ? request.role().toUpperCase().trim()
                : "USER";

        String passwordHash = passwordEncoder.encode(request.password());
        Instant now = Instant.now();

        User user = new User(
                UUID.randomUUID(),
                normalizedEmail,
                passwordHash,
                request.fullName().trim(),
                role,
                now,
                now
        );

        User savedUser = userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getEmail());
        UserSummaryDto userSummary = new UserSummaryDto(savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole());

        return AuthResponseDto.of(accessToken, refreshToken, tokenProvider.getAccessExpirationMs(), userSummary);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.email().toLowerCase().trim();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole());

        return AuthResponseDto.of(accessToken, refreshToken, tokenProvider.getAccessExpirationMs(), userSummary);
    }

    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        if (!tokenProvider.validateToken(request.refreshToken())) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        UUID userId = tokenProvider.getUserId(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String accessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        UserSummaryDto userSummary = new UserSummaryDto(user.getId(), user.getEmail(), user.getFullName(), user.getRole());

        return AuthResponseDto.of(accessToken, refreshToken, tokenProvider.getAccessExpirationMs(), userSummary);
    }

    public UserProfileResponseDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new UserProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Transactional
    public UserProfileResponseDto anonymizeAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        String pseudoSuffix = user.getId().toString().substring(0, 8);
        user.setFullName("Native-" + pseudoSuffix);
        user.setEmail("anonymous-" + pseudoSuffix + "@privacy.local");
        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);
        return new UserProfileResponseDto(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole(),
                saved.getCreatedAt()
        );
    }
}
