package com.banking.cards.service.admin;

import com.banking.cards.common.Role;
import com.banking.cards.entity.User;
import com.banking.cards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() == newRole) {
            return;
        }

        user.setRole(newRole);
    }
}
