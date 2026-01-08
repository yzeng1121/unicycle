package com.unicycle.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.exception.FailedToFetchUsernameException;
import com.unicycle.exception.FailedToFetchUserIdException;
import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.UserNotFoundException;

import java.util.Optional;
import java.util.UUID;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    // TODO: i made find to lower case so check all other to see if it matches w the entire auth flow
    @Override
    public UserDetails loadUserByUsername(String username) {
        if (username == null) throw new InvalidCredentialsException("Username is null.");
        String usernameStriped = username.replaceAll("\\s+", "");

        if (!verifyUsername(usernameStriped)) {
            throw new InvalidCredentialsException("Username is invalid.");
        }

        try {
            Optional<User> user = userRepository.findByUsername(usernameStriped.toLowerCase());
            return user.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FailedToFetchUsernameException("Failed to fetch user with username.");
        }
    }

    private boolean verifyUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_.]+$";
        return username.matches(usernameRegex) && username.length() >= 4 && username.length() <= 16;
    }

    public UserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        if (userId == null) throw new InvalidCredentialsException("UserId is null.");
        Optional<User> user = userRepository.findByUserId(userId);

        try {
            return user.orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new FailedToFetchUserIdException("Failed to fetch user with user id.");
        }
    }
}