package com.gymnetwork.wallet.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import com.gymnetwork.wallet.dto.request.RefundWalletRequest;
import com.gymnetwork.wallet.dto.response.InvoiceResponse;
import com.gymnetwork.wallet.dto.response.WalletLedgerDto;
import com.gymnetwork.wallet.dto.response.WalletResponse;
import com.gymnetwork.wallet.service.WalletService;
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
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "User Wallet and Ledger APIs")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    @Operation(summary = "Get user wallet details and balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWallet(principal.getId())));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated wallet transaction history")
    public ResponseEntity<ApiResponse<PageResponse<WalletLedgerDto>>> getWalletHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletHistory(principal.getId(), PageRequest.of(page, size))));
    }
    
    @GetMapping("/transactions")
    @Operation(summary = "Alias for wallet history")
    public ResponseEntity<ApiResponse<PageResponse<WalletLedgerDto>>> getWalletTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return getWalletHistory(principal, page, size);
    }

    @PostMapping("/recharge")
    @Operation(summary = "Recharge wallet balance")
    public ResponseEntity<ApiResponse<WalletResponse>> rechargeWallet(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RechargeWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Wallet recharged successfully", walletService.rechargeWallet(principal.getId(), request)));
    }

    @PostMapping("/refund")
    @Operation(summary = "Process refund to wallet")
    public ResponseEntity<ApiResponse<WalletResponse>> processRefund(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RefundWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", walletService.processRefund(principal.getId(), request)));
    }

    @GetMapping("/invoice/{transactionId}")
    @Operation(summary = "Generate invoice for a wallet transaction")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(ApiResponse.success("Invoice generated successfully", walletService.generateInvoice(principal.getId(), transactionId)));
    }
}
