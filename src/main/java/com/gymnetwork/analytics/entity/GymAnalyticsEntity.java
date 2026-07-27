package com.gymnetwork.analytics.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gym_analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymAnalyticsEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false, unique = true)
    private UUID gymId;

    @Column(name = "total_check_ins", nullable = false)
    @Builder.Default
    private Long totalCheckIns = 0L;

    @Column(name = "total_earnings", nullable = false)
    @Builder.Default
    private BigDecimal totalEarnings = BigDecimal.ZERO;
    
    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Long totalReviews = 0L;
    
    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;
}
