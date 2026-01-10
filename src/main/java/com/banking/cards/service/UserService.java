package com.banking.cards.service;

import com.banking.cards.common.Role;
import com.banking.cards.dto.request.LoginRequest;
import com.banking.cards.entity.User;
import com.banking.cards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UUID register(LoginRequest request) {

         if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        return userRepository.save(user).getUniqueKey();
    }
}