package com.gymnetwork.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String phoneNumber;
    private String role;
    private String firstName;
    private String lastName;
    private String status;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}
