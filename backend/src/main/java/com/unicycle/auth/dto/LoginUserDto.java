package com.unicycle.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDto {
    // TODO: i want so users can also login with username
    private String email;
    private String password;
}
