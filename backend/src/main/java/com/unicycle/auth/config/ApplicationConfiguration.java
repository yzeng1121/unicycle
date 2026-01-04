package com.unicycle.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.unicycle.auth.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Configuration
public class ApplicationConfiguration {
    private final UserRepository userRepository;

    // user lookup service Spring Security uses to find user information during login
    // if user is not found an exception is thrown
    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    // encodes password securely 
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // main controll of user authentication
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // creates the actual checker that does the work of checking
    // tells spring how to verify login credentials
    @Bean
    AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
