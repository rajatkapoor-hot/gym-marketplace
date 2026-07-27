package com.gymnetwork.gym.dto.response;

import com.gymnetwork.shared.enums.PassType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPricingDto {
    private UUID id;
    private PassType passType;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private Boolean isActive;
}
