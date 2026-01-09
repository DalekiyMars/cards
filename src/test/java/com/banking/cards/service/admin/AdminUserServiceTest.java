package com.banking.cards.service.admin;

import com.banking.cards.common.audit.AuditAction;
import com.banking.cards.common.audit.AuditEntityType;
import com.banking.cards.entity.AuditLog;
import com.banking.cards.repository.AuditLogRepository;
import com.banking.cards.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Captor
    private ArgumentCaptor<AuditLog> auditLogCaptor;

    @InjectMocks
    private AuditService auditService;

    private UUID testActorId;

    @BeforeEach
    void setUp() {
        testActorId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("log - Успешное логирование аудита")
    void log_shouldSaveAuditLog() {
        // Arrange
        setupAuthentication(testActorId);

        // Act
        auditService.log(
                AuditAction.USER_ROLE_CHANGED,
                AuditEntityType.USER,
                "550e8400-e29b-41d4-a716-446655440000",
                "got new role =ADMIN"
        );

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(AuditAction.USER_ROLE_CHANGED);
        assertThat(savedLog.getEntityType()).isEqualTo(AuditEntityType.USER);
        assertThat(savedLog.getEntityId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(savedLog.getDetails()).isEqualTo("got new role =ADMIN");
        assertThat(savedLog.getActorUserId()).isEqualTo(testActorId.toString());
        assertThat(savedLog.getActorRole()).isEqualTo("ROLE_ADMIN");
        assertThat(savedLog.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("currentUserId - Возвращает строку при string principal")
    void currentUserId_shouldReturnString_whenPrincipalIsString() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("admin_user");

        // Act
        String result = auditService.currentUserId();

        // Assert
        assertThat(result).isEqualTo("admin_user");
    }

    @Test
    @DisplayName("currentUserId - Бросает исключение при отсутствии аутентификации")
    void currentUserId_shouldThrowException_whenNotAuthenticated() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> auditService.currentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user for audit");
    }

    @Test
    @DisplayName("currentUserId - Бросает исключение при неподдерживаемом типе principal")
    void currentUserId_shouldThrowException_whenPrincipalTypeUnsupported() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(12345); // Integer - неподдерживаемый тип

        // Act & Assert
        assertThatThrownBy(() -> auditService.currentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported principal type");
    }

    @Test
    @DisplayName("log - Обрабатывает аудит для разных действий")
    void log_shouldHandleDifferentAuditActions() {
        // Arrange
        setupAuthentication(testActorId);

        // Тестируем разные действия
        testAuditAction(AuditAction.CARD_CREATED, AuditEntityType.CARD);
        testAuditAction(AuditAction.CARD_DELETED, AuditEntityType.CARD);
        testAuditAction(AuditAction.CARD_STATUS_CHANGED, AuditEntityType.CARD);
        testAuditAction(AuditAction.USER_ROLE_CHANGED, AuditEntityType.USER);
    }

    private void testAuditAction(AuditAction action, AuditEntityType entityType) {
        // Act
        auditService.log(
                action,
                entityType,
                "test-entity-id",
                "test details"
        );

        // Assert
        verify(auditLogRepository, atLeastOnce()).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getAction()).isEqualTo(action);
        assertThat(savedLog.getEntityType()).isEqualTo(entityType);

        // Сброс счетчика для следующего теста
        reset(auditLogRepository);
    }

    @Test
    @DisplayName("log - Сохраняет все переданные детали")
    void log_shouldSaveAllProvidedDetails() {
        // Arrange
        setupAuthentication(testActorId);
        String longDetails = "User changed role from USER to ADMIN. Performed by admin with ID: " +
                testActorId + " at " + System.currentTimeMillis();

        // Act
        auditService.log(
                AuditAction.USER_ROLE_CHANGED,
                AuditEntityType.USER,
                "user-id-123",
                longDetails
        );

        // Assert
        verify(auditLogRepository).save(auditLogCaptor.capture());

        AuditLog savedLog = auditLogCaptor.getValue();
        assertThat(savedLog.getDetails()).isEqualTo(longDetails);
        assertThat(savedLog.getDetails()).hasSizeGreaterThan(50); // Проверяем, что детали сохраняются полностью
    }

    private void setupAuthentication(UUID userId) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);
    }
}