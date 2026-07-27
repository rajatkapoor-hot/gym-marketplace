package com.gymnetwork.payment.repository;

import com.gymnetwork.payment.entity.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentEntity p where p.razorpayOrderId = :razorpayOrderId")
    Optional<PaymentEntity> findByRazorpayOrderIdForUpdate(@Param("razorpayOrderId") String razorpayOrderId);
    Page<PaymentEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
