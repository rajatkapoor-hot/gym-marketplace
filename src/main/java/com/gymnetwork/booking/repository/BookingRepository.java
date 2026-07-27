package com.gymnetwork.booking.repository;

import com.gymnetwork.booking.entity.BookingEntity;
import com.gymnetwork.shared.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    Page<BookingEntity> findByUserIdOrderByBookingDateDesc(UUID userId, Pageable pageable);
    Page<BookingEntity> findByGymIdOrderByBookingDateDesc(UUID gymId, Pageable pageable);
    List<BookingEntity> findByUserIdAndStatusInOrderByBookingDateAscEntryTimeAsc(UUID userId, Collection<BookingStatus> statuses);
    
    boolean existsByUserIdAndGymIdAndBookingDate(UUID userId, UUID gymId, LocalDate bookingDate);
}
