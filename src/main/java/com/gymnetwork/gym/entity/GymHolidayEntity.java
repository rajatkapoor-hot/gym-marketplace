package com.gymnetwork.gym.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gym_holidays")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymHolidayEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "reason")
    private String reason;
}
