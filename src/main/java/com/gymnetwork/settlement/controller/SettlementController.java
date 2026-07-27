package com.gymnetwork.settlement.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.settlement.dto.response.SettlementLedgerResponse;
import com.gymnetwork.settlement.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlement Management", description = "Gym Owner Settlements APIs")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/owner")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Get gym owner's settlement ledgers")
    public ResponseEntity<ApiResponse<PageResponse<SettlementLedgerResponse>>> getOwnerSettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.getOwnerSettlements(principal.getId(), PageRequest.of(page, size))));
    }
}
