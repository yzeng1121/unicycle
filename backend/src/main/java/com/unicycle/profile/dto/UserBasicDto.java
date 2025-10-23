package com.unicycle.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicDto {
    private String username;
    private String firstName;
    private String lastName;
    private String dorm;
}
