package com.banking.cards.util;

import com.banking.cards.common.Role;
import com.banking.cards.config.AdminProperties;
import com.banking.cards.entity.User;
import com.banking.cards.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProperties;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            AdminProperties adminProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminProperties = adminProperties;
    }

    @PostConstruct
    public void initAdminUser() {
        try {
            final var username = adminProperties.getUsername();
            if (!userRepository.existsByUsername(username)) {
                User admin = new User();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                log.info("Администратор создан: {}", adminProperties.getUsername());
            } else {
                log.info("Администратор уже существует: {}", adminProperties.getUsername());
            }
        } catch (Exception e) {
            log.error("Ошибка при создании администратора", e);
        }
    }
}
