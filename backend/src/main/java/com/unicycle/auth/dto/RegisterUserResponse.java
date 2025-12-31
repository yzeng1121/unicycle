package com.unicycle.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.entity.Profile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    private UserBasicDto user;
    private Profile profile;
}
