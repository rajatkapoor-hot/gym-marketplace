package com.gymnetwork.gym.dto.response;

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
public class GymResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String addressLine1;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String status;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private String coverImageUrl;
}
