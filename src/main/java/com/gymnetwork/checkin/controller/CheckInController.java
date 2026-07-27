package com.gymnetwork.checkin.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.checkin.dto.request.CheckInRequest;
import com.gymnetwork.checkin.dto.response.CheckInResponse;
import com.gymnetwork.checkin.service.CheckInService;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@Tag(name = "Check-In Management", description = "User Check-in APIs via QR Code")
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    @Operation(summary = "Process user check-in using QR data")
    public ResponseEntity<ApiResponse<CheckInResponse>> processCheckIn(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Check-in successful", checkInService.processCheckIn(principal.getId(), request)));
    }

    @GetMapping("/user")
    @Operation(summary = "Get user's check-in history")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getUserCheckIns(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(checkInService.getUserCheckIns(principal.getId(), PageRequest.of(page, size))));
    }

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get gym's check-in history (Owner/Admin)")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getGymCheckIns(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(checkInService.getGymCheckIns(principal.getId(), gymId, PageRequest.of(page, size))));
    }
}
