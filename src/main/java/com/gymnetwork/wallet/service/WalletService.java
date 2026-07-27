package com.gymnetwork.wallet.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import com.gymnetwork.wallet.dto.request.RefundWalletRequest;
import com.gymnetwork.wallet.dto.response.InvoiceResponse;
import com.gymnetwork.wallet.dto.response.WalletLedgerDto;
import com.gymnetwork.wallet.dto.response.WalletResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WalletService {
    WalletResponse getWallet(UUID userId);
    PageResponse<WalletLedgerDto> getWalletHistory(UUID userId, Pageable pageable);
    WalletResponse rechargeWallet(UUID userId, RechargeWalletRequest request);
    WalletResponse processRefund(UUID userId, RefundWalletRequest request);
    InvoiceResponse generateInvoice(UUID userId, UUID transactionId);
}
