package com.banking.cards.service;

import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.entity.AuditLog;
import com.banking.cards.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            AuditAction action,
            AuditEntityType entityType,
            String entityId,
            String details
    ) {

        AuditLog log = AuditLog.builder()
                .actorUserId(currentUserId())
                .actorRole(currentRole())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .createdAt(Instant.now())
                .build();

        auditLogRepository.save(log);
    }

    public String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user for audit");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UUID uuid) {
            return uuid.toString();
        } else if (principal instanceof String string) {
            return string;
        }

        throw new IllegalStateException(
                "Unsupported principal type: " + principal.getClass()
        );
    }

    private String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("UNKNOWN");
    }
}
