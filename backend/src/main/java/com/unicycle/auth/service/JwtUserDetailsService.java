package com.unicycle.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unicycle.auth.entity.User;
import com.unicycle.auth.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("inside loadUserByUsername:: " + email);
        
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public UserDetails loadUserByUserId(UUID userId) throws UsernameNotFoundException {
        System.out.println("inside loadUserByUserId:: " + userId);
        
        Optional<User> user = userRepository.findByUserId(userId);
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}