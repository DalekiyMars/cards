package com.banking.cards.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    // 1. Определяем точку среза: все классы с аннотацией @RestController
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {}

    // 2. Логика, выполняемая ПЕРЕД (Before) вызовом метода
    @Before("restControllerMethods()")
    public void logUserAccess(JoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(auth) || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return;
        }

        // Извлекаем данные
        String username = auth.getName();
        String roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        // Логируем
        log.info("ACCESS AUDIT | User: {} | Roles: [{}] | Method: {} with args: [{}]",
                username, roles, methodName, args);
    }
}