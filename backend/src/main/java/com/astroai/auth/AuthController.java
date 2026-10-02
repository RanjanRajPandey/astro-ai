package com.astroai.auth;

import com.astroai.auth.dto.AuthResponseDto;
import com.astroai.auth.dto.LoginRequestDto;
import com.astroai.auth.dto.RefreshTokenRequestDto;
import com.astroai.auth.dto.RegisterRequestDto;
import com.astroai.auth.dto.UserProfileResponseDto;
import com.astroai.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("User login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        AuthResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("Token refresh successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getCurrentUser(
            @AuthenticationPrincipal AstroUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }

        UserProfileResponseDto profile = authService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("User profile retrieved", profile));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal AstroUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        authService.deleteAccount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("User account and all personal birth data deleted successfully", null));
    }

    @PostMapping("/me/anonymize")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> anonymizeAccount(
            @AuthenticationPrincipal AstroUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated"));
        }
        UserProfileResponseDto response = authService.anonymizeAccount(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.ok("Account data anonymized successfully for privacy compliance", response));
    }
}
