package com.gymnetwork.review.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.review.dto.request.ReviewRequest;
import com.gymnetwork.review.dto.response.ReviewResponse;
import com.gymnetwork.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "Gym Reviews and Ratings APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Add a review for a gym")
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review added successfully", reviewService.addReview(principal.getId(), request)));
    }

    @GetMapping("/gym/{gymId}")
    @Operation(summary = "Get paginated reviews for a gym")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getGymReviews(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getGymReviews(gymId, PageRequest.of(page, size))));
    }
}
