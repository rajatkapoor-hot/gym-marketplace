package com.gymnetwork.checkin.repository;

import com.gymnetwork.checkin.entity.CheckInEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CheckInRepositoryTest {

    @Autowired
    private CheckInRepository checkInRepository;

    @Test
    void cannotCommitTwoCheckInsForSameBooking() {
        UUID bookingId = UUID.randomUUID();

        checkInRepository.saveAndFlush(checkIn(bookingId));

        assertThatThrownBy(() -> checkInRepository.saveAndFlush(checkIn(bookingId)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private CheckInEntity checkIn(UUID bookingId) {
        return CheckInEntity.builder()
                .bookingId(bookingId)
                .gymId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .checkInTime(LocalDateTime.now())
                .build();
    }
}
