package com.gymnetwork.checkin.repository;

import com.gymnetwork.checkin.entity.CheckInEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckInEntity, UUID> {
    Page<CheckInEntity> findByUserIdOrderByCheckInTimeDesc(UUID userId, Pageable pageable);
    Page<CheckInEntity> findByGymIdOrderByCheckInTimeDesc(UUID gymId, Pageable pageable);
    Optional<CheckInEntity> findByBookingId(UUID bookingId);
    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);
}
