package com.astroai.birth;

import com.astroai.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/birth-profiles")
public class BirthProfileController {

    private final BirthProfileService birthProfileService;

    public BirthProfileController(BirthProfileService birthProfileService) {
        this.birthProfileService = birthProfileService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BirthProfileResponse>> createProfile(
            @Valid @RequestBody BirthProfileRequest request
    ) {
        BirthProfileResponse created = birthProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Birth profile created successfully", created));
    }

    @GetMapping
    public ApiResponse<List<BirthProfileResponse>> listProfiles(
            @RequestParam(name = "userId", required = false) UUID userId
    ) {
        return ApiResponse.ok(birthProfileService.listProfiles(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<BirthProfileResponse> getProfile(@PathVariable UUID id) {
        return ApiResponse.ok(birthProfileService.getProfile(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<BirthProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody BirthProfileRequest request
    ) {
        return ApiResponse.ok("Birth profile updated successfully", birthProfileService.updateProfile(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProfile(@PathVariable UUID id) {
        birthProfileService.deleteProfile(id);
        return ApiResponse.ok("Birth profile deleted successfully", null);
    }
}
