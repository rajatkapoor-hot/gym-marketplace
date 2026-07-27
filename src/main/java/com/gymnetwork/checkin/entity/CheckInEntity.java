package com.gymnetwork.checkin.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "checkins", uniqueConstraints = {
        @UniqueConstraint(name = "uk_checkins_booking_id", columnNames = "booking_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "SUCCESS";
}
