package com.gymnetwork.user.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String emergencyContactPhone;
}
