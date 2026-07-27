package com.gymnetwork.wallet.service.impl;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.shared.service.WalletInternalService;
import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import com.gymnetwork.wallet.dto.request.RefundWalletRequest;
import com.gymnetwork.wallet.dto.response.InvoiceResponse;
import com.gymnetwork.wallet.dto.response.WalletLedgerDto;
import com.gymnetwork.wallet.dto.response.WalletResponse;
import com.gymnetwork.wallet.entity.WalletEntity;
import com.gymnetwork.wallet.entity.WalletLedgerEntity;
import com.gymnetwork.wallet.repository.WalletLedgerRepository;
import com.gymnetwork.wallet.repository.WalletRepository;
import com.gymnetwork.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService, WalletInternalService {

    private final WalletRepository walletRepository;
    private final WalletLedgerRepository walletLedgerRepository;

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId) {
        WalletEntity wallet = getWalletEntityByUserId(userId);
        return mapToResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WalletLedgerDto> getWalletHistory(UUID userId, Pageable pageable) {
        WalletEntity wallet = getWalletEntityByUserId(userId);
        Page<WalletLedgerEntity> ledgerPage = walletLedgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
        return PageResponse.from(ledgerPage.map(this::mapToLedgerDto));
    }

    @Override
    @Transactional
    public WalletResponse rechargeWallet(UUID userId, RechargeWalletRequest request) {
        WalletEntity wallet = getWalletEntityByUserIdForUpdate(userId);
        String referenceId = request.getPaymentReferenceId();
        String category = "RECHARGE";

        if (Objects.nonNull(referenceId)
                && walletLedgerRepository.existsByWalletIdAndReferenceIdAndCategory(wallet.getId(), referenceId, category)) {
            log.info("Skipping duplicate wallet recharge for walletId={}, referenceId={}, category={}",
                    wallet.getId(), referenceId, category);
            return mapToResponse(wallet);
        }
        
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        
        WalletEntity updatedWallet = walletRepository.save(wallet);
        
        WalletLedgerEntity ledger = WalletLedgerEntity.builder()
                .walletId(updatedWallet.getId())
                .referenceId(referenceId)
                .type("CREDIT")
                .category(category)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description("Wallet recharge")
                .build();
        walletLedgerRepository.save(ledger);
        
        return mapToResponse(updatedWallet);
    }

    @Override
    @Transactional
    public WalletResponse processRefund(UUID userId, RefundWalletRequest request) {
        WalletEntity wallet = getWalletEntityByUserId(userId);
        
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        
        WalletEntity updatedWallet = walletRepository.save(wallet);
        
        WalletLedgerEntity ledger = WalletLedgerEntity.builder()
                .walletId(updatedWallet.getId())
                .referenceId(request.getBookingId().toString())
                .type("CREDIT")
                .category("REFUND")
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description("Refund for booking: " + request.getReason())
                .build();
        walletLedgerRepository.save(ledger);
        
        return mapToResponse(updatedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse generateInvoice(UUID userId, UUID transactionId) {
        WalletEntity wallet = getWalletEntityByUserId(userId);
        WalletLedgerEntity ledger = walletLedgerRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
                
        if (!ledger.getWalletId().equals(wallet.getId())) {
            throw new BadRequestException("Transaction does not belong to this wallet");
        }
        
        return InvoiceResponse.builder()
                .transactionId(ledger.getId())
                .walletId(wallet.getId())
                .invoiceNumber("INV-" + ledger.getId().toString().substring(0, 8).toUpperCase())
                .date(ledger.getCreatedAt())
                .transactionType(ledger.getType())
                .category(ledger.getCategory())
                .amount(ledger.getAmount())
                .description(ledger.getDescription())
                .userDetails(InvoiceResponse.UserDetails.builder()
                        .name("User") // In a real scenario, fetch from UserProfileService
                        .email("user@example.com")
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void createWalletForUser(UUID userId) {
        if (walletRepository.findByUserId(userId).isEmpty()) {
            WalletEntity wallet = WalletEntity.builder()
                    .userId(userId)
                    .balance(BigDecimal.ZERO)
                    .currency("INR")
                    .status("ACTIVE")
                    .build();
            walletRepository.save(wallet);
        }
    }

    @Override
    @Transactional
    public void deductWallet(UUID userId, BigDecimal amount, String referenceId, String description) {
        WalletEntity wallet = getWalletEntityByUserId(userId);
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient wallet balance for this transaction");
        }
        
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        
        WalletEntity updatedWallet = walletRepository.save(wallet);
        
        WalletLedgerEntity ledger = WalletLedgerEntity.builder()
                .walletId(updatedWallet.getId())
                .referenceId(referenceId)
                .type("DEBIT")
                .category("BOOKING")
                .amount(amount)
                .balanceAfter(newBalance)
                .description(description)
                .build();
        walletLedgerRepository.save(ledger);
    }

    private WalletEntity getWalletEntityByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    private WalletEntity getWalletEntityByUserIdForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    private WalletResponse mapToResponse(WalletEntity wallet) {
        return WalletResponse.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .build();
    }
    
    private WalletLedgerDto mapToLedgerDto(WalletLedgerEntity ledger) {
        return WalletLedgerDto.builder()
                .id(ledger.getId())
                .walletId(ledger.getWalletId())
                .referenceId(ledger.getReferenceId())
                .type(ledger.getType())
                .category(ledger.getCategory())
                .amount(ledger.getAmount())
                .balanceAfter(ledger.getBalanceAfter())
                .description(ledger.getDescription())
                .createdAt(ledger.getCreatedAt())
                .build();
    }
}
