package com.unicycle.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    // TODO: what other information should be required?
    // TODO: consider adding more components, maybe grad year just to name one
    // TODO: major would be hella funny
    private String firstName;
    private String lastName;
    private String dorm;
    private String email;
    private String password;
    private String username;
}
