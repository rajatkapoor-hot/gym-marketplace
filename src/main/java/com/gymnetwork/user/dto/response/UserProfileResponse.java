package com.gymnetwork.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String emergencyContactPhone;
}
