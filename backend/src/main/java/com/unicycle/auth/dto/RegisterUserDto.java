package com.unicycle.auth.dto;

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
