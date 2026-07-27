package com.gymnetwork.gym.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gym_badges")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymBadgeEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "badge_type", nullable = false)
    private String badgeType; // FEATURED, TOP_RATED, VERIFIED, PREMIUM

    @Column(name = "granted_at", nullable = false)
    @Builder.Default
    private Instant grantedAt = Instant.now();
}
