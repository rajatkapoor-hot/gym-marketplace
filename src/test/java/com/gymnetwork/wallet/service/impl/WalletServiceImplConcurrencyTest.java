package com.gymnetwork.wallet.service.impl;

import com.gymnetwork.BaseIntegrationTest;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.wallet.entity.WalletEntity;
import com.gymnetwork.wallet.entity.WalletLedgerEntity;
import com.gymnetwork.wallet.repository.WalletLedgerRepository;
import com.gymnetwork.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class WalletServiceImplConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    private WalletServiceImpl walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletLedgerRepository walletLedgerRepository;

    @BeforeEach
    void cleanDatabase() {
        walletLedgerRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void parallelDebitAttemptsCannotOverdrawWallet() throws Exception {
        UUID userId = UUID.randomUUID();
        WalletEntity wallet = walletRepository.save(WalletEntity.builder()
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .currency("INR")
                .status("ACTIVE")
                .build());

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<DebitResult>> futures = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            String referenceId = "parallel-checkin-" + i;
            futures.add(executor.submit(debitAfterSignal(userId, new BigDecimal("70.00"), referenceId, ready, start)));
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();

        List<DebitResult> results = new ArrayList<>();
        for (Future<DebitResult> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdownNow();

        assertThat(results).filteredOn(DebitResult::success).hasSize(1);
        assertThat(results).filteredOn(result -> !result.success()).hasSize(1);
        assertThat(results).filteredOn(result -> !result.success())
                .allSatisfy(result -> assertThat(result.message()).contains("Insufficient wallet balance"));

        WalletEntity updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isEqualByComparingTo("30.00");

        List<WalletLedgerEntity> ledgers = walletLedgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        assertThat(ledgers).hasSize(1);
        assertThat(ledgers.getFirst().getCategory()).isEqualTo("CHECKIN_DEDUCTION");
        assertThat(ledgers.getFirst().getBalanceAfter()).isEqualByComparingTo("30.00");
    }

    @Test
    void duplicateDebitAttemptsCannotCreateExtraLedgerOrNegativeBalance() throws Exception {
        UUID userId = UUID.randomUUID();
        WalletEntity wallet = walletRepository.save(WalletEntity.builder()
                .userId(userId)
                .balance(new BigDecimal("50.00"))
                .currency("INR")
                .status("ACTIVE")
                .build());

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<DebitResult>> futures = List.of(
                executor.submit(debitAfterSignal(userId, new BigDecimal("50.00"), "duplicate-checkin", ready, start)),
                executor.submit(debitAfterSignal(userId, new BigDecimal("50.00"), "duplicate-checkin", ready, start))
        );

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();

        List<DebitResult> results = new ArrayList<>();
        for (Future<DebitResult> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdownNow();

        assertThat(results).filteredOn(DebitResult::success).hasSize(1);
        assertThat(results).filteredOn(result -> !result.success()).hasSize(1);

        WalletEntity updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updatedWallet.getBalance()).isZero();

        List<WalletLedgerEntity> ledgers = walletLedgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        assertThat(ledgers).hasSize(1);
        assertThat(ledgers.getFirst().getReferenceId()).isEqualTo("duplicate-checkin");
    }

    private Callable<DebitResult> debitAfterSignal(
            UUID userId,
            BigDecimal amount,
            String referenceId,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            start.await(5, TimeUnit.SECONDS);
            try {
                walletService.deductWallet(userId, amount, referenceId, "Concurrent check-in deduction");
                return new DebitResult(true, null);
            } catch (BadRequestException ex) {
                return new DebitResult(false, ex.getMessage());
            }
        };
    }

    private record DebitResult(boolean success, String message) {
    }
}
