package com.unicycle.auth.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.unicycle.auth.service.JwtService;
import com.unicycle.auth.service.JwtUserDetailsService;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;

// TODO: test all the components of the class
@AllArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;

    // security checkpoint that runs before every request to app
    // checks if user has a valid JWT access token & either lets them through/blocks them
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain 
    ) throws ServletException, IOException {
        // TODO: remove later after debugging
        System.out.println("=== JWT Filter Debug ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        // 1. checks the JWT 
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. extracts the JWT 
            final String jwt = authHeader.substring(7);
            final UUID userId = jwtService.extractUserId(jwt);

            // 3. check if JWT already authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            System.out.println("JWT found, userId: " + userId);
            System.out.println("Current authentication: " + authentication);

            if (userId != null && authentication == null) {
                UserDetails userDetails = this.jwtUserDetailsService.loadUserByUserId(userId);

                System.out.println("UserDetails userId: " + userDetails.getUsername());
                System.out.println("Token userId: " + userId);

                // 4. validate the JWT
                // TODO: this is not being passed through
                if (jwtService.isAccessToken(jwt) && jwtService.isTokenValid(jwt, userId)) {

                    // 5. grant user access
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("New authentication: " + SecurityContextHolder.getContext().getAuthentication());
                    System.out.println("User principal: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                    System.out.println("JWT validated, user authenticated");
                }
            }

            System.out.println("Continuing filter chain after JWT processing");
        } catch (Exception exception) {
            // TODO: lsot error context
            System.out.println("JWT Filter Exception: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            SecurityContextHolder.clearContext();
            return;
        }
        filterChain.doFilter(request, response);
    }
}
