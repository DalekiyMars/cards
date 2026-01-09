package com.banking.cards.service.admin;

import com.banking.cards.common.Role;
import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.entity.User;
import com.banking.cards.repository.UserRepository;
import com.banking.cards.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public void changeUserRole(UUID userId, Role newRole) {
        User user = userRepository.findByUniqueKey(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getRole() == newRole) {
            return;
        }

        auditService.log(
                AuditAction.USER_ROLE_CHANGED,
                AuditEntityType.USER,
                userId.toString(),
                "got new role =" + newRole
        );

        user.setRole(newRole);
    }
}
