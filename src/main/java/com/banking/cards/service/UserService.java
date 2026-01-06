package com.banking.cards.service;

import com.banking.cards.common.Role;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.dto.request.LoginRequest;
import com.banking.cards.entity.User;
import com.banking.cards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public void register(LoginRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        auditService.log(
                AuditAction.USER_REGISTERED,
                AuditEntityType.USER,
                user.getUniqueKey(),
                "new user registered: " + user
        );

        userRepository.save(user);
    }
}