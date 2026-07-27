package com.gymnetwork.user.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.user.dto.request.UpdateUserProfileRequest;
import com.gymnetwork.user.dto.response.UserProfileResponse;
import com.gymnetwork.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User Profile and Favorites Management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getId())));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile details")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                          @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userService.updateProfile(principal.getId(), request)));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "Soft delete user account")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@AuthenticationPrincipal UserPrincipal principal) {
        userService.deleteProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @GetMapping("/history")
    @Operation(summary = "Get user activity/booking history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserHistory(principal.getId())));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get user active bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUserBookings(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserBookings(principal.getId())));
    }

    @GetMapping("/favourites")
    @Operation(summary = "Get list of favourite gym IDs")
    public ResponseEntity<ApiResponse<List<UUID>>> getFavourites(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getFavourites(principal.getId())));
    }

    @PostMapping("/favourites/{gymId}")
    @Operation(summary = "Add gym to user favourites")
    public ResponseEntity<ApiResponse<Void>> addFavourite(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable UUID gymId) {
        userService.addFavourite(principal.getId(), gymId);
        return ResponseEntity.ok(ApiResponse.success("Gym added to favourites", null));
    }

    @DeleteMapping("/favourites/{gymId}")
    @Operation(summary = "Remove gym from user favourites")
    public ResponseEntity<ApiResponse<Void>> removeFavourite(@AuthenticationPrincipal UserPrincipal principal,
                                                              @PathVariable UUID gymId) {
        userService.removeFavourite(principal.getId(), gymId);
        return ResponseEntity.ok(ApiResponse.success("Gym removed from favourites", null));
    }
}
