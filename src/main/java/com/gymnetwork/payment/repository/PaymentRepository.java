package com.gymnetwork.payment.repository;

import com.gymnetwork.payment.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);
    Page<PaymentEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
