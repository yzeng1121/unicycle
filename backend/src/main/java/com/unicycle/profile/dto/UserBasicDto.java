package com.unicycle.profile.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicDto {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String dorm;
}
