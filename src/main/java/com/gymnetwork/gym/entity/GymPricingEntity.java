package com.gymnetwork.gym.entity;

import com.gymnetwork.common.entity.BaseEntity;
import com.gymnetwork.shared.enums.PassType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "gym_pricing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPricingEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_type", nullable = false)
    private PassType passType;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "discounted_price", nullable = false)
    private BigDecimal discountedPrice;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
