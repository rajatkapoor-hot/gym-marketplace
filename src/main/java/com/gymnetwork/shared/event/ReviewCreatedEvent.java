package com.gymnetwork.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreatedEvent {
    private UUID gymId;
    private Double newAverageRating;
    private Integer totalReviews; // We might need to query this or just pass new average
}
