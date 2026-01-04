package com.unicycle.auth.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // disables csrf protection, authorize all HTTP requests w/ the auth header,
    // require authentication for anything else that doesn't match the header,
    // sets session management to stateless, every request is treated as a new 
    // one even if its coming from the same client or if received earlier, add
    // authentication provider + filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/listings/**").authenticated()
                .requestMatchers("/profiles/**").authenticated()
                .requestMatchers("/error").permitAll() // TODO: remove later for handling error fetching user w/ userID
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // TODO: change the host of the backend to what I'll be using
    // TODO: figure out what to do with localhost:8080 (make unique)
    // tells API which websites are allowed to be requests to it
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // WHO can access the API
        configuration.setAllowedOrigins(List.of("exp://10.243.122.160:8081", "exp://Yuxins-Mac.local:8081", "http://localhost:8080"));

        // WHAT actions they can do
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // WHAT headers they can send
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // apply these rules to ALL endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
