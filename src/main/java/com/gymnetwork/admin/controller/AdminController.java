package com.gymnetwork.admin.controller;

import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.gym.dto.response.GymResponse;
import com.gymnetwork.gym.service.GymService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Platform Administration APIs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final GymService gymService;
    

    @GetMapping("/gyms")
    @Operation(summary = "Get all gyms in the platform")
    public ResponseEntity<ApiResponse<PageResponse<GymResponse>>> getAllGyms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getAllGyms(PageRequest.of(page, size))));
    }

    // Usually there would be an AdminService, but for brevity, we can delegate to existing services 
    // or add specific admin-only methods to them if needed (e.g., approve gym, suspend user).
    // Let's add a placeholder for getting all users. We don't have getAllUsers in UserProfileService yet, 
    // so let's skip it or implement it.
}
