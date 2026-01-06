package com.unicycle.auth.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.unicycle.auth.dto.LoginUserDto;
import com.unicycle.auth.dto.RegisterUserDto;
import com.unicycle.auth.dto.RegisterUserResponse;
import com.unicycle.auth.dto.VerifyUserDto;
import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;

import com.unicycle.exception.InvalidCredentialsException;
import com.unicycle.exception.UserNotFoundException;
import com.unicycle.exception.UserNotVerifiedException;
import com.unicycle.exception.UsernameAlreadyExistsException;
import com.unicycle.profile.dto.UserBasicDto;
import com.unicycle.profile.entity.Profile;
import com.unicycle.profile.service.ProfileService;
import com.unicycle.exception.InvalidVerificationException;
import com.unicycle.exception.UserAlreadyVerifiedException;
import com.unicycle.exception.ExpiredVerificationException;
import com.unicycle.exception.EmailAlreadyExistsException;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ProfileService profileService;
    private final EmailService emailService;

    private static final int ACCESS_TOKEN_EXPIRY_MINUTES = 15;

    @Transactional
    public RegisterUserResponse signup(RegisterUserDto input) throws MessagingException {
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists.");
        }
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        User user = User.builder()
            .firstName(input.getFirstName())
            .lastName(input.getLastName())
            .dorm(input.getDorm())
            .username(input.getUsername())
            .email(input.getEmail())
            .password(passwordEncoder.encode(input.getPassword()))
            .build();
        setNewVerificationCode(user);
        user.setEnabled(false);

        sendVerificationEmail(user);
        user = userRepository.save(user);

        UserBasicDto dto = UserBasicDto.builder()
            .userId(user.getUserId())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .dorm(user.getDorm())
            .build();

        Profile initializedProfile = profileService.createProfile(user.getUserId());

        RegisterUserResponse response = new RegisterUserResponse(dto, initializedProfile);
        return response;
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("Account not verified. Please verify your account.");
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.getEmail(),
                input.getPassword()
            )
        );
        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        User user = getUserByEmailOrThrow(input.getEmail());
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ExpiredVerificationException("Verification code has expired.");
        }

        if (user.getVerificationCode().equals(input.getVerificationCode())) {
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiresAt(null);
            userRepository.save(user);
        } else {
            throw new InvalidVerificationException("Invalid verification code.");
        }
    }

    public void resendVerificationCode(String email) throws MessagingException {
        User user = getUserByEmailOrThrow(email);
        if (user.isEnabled()) {
            throw new UserAlreadyVerifiedException("Account is already verified.");
        }
        setNewVerificationCode(user);
        sendVerificationEmail(user);
        userRepository.save(user);
    }

    // TODO customize message and UI below
    public void sendVerificationEmail(User user) throws MessagingException {
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // TODO: for my own program figure out a better way to troubleshoot
            throw new MessagingException("Failed to send verification code to user email.");
        }
    }

    // TODO: customize this function to account for the number of students
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 10000; 
        return String.valueOf(code);
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private void setNewVerificationCode(User user) {
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(ACCESS_TOKEN_EXPIRY_MINUTES));
    }
}
