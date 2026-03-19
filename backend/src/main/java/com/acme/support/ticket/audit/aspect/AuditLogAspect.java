package com.acme.support.ticket.audit.aspect;

import com.acme.support.ticket.audit.annotation.AuditLog;
import com.acme.support.ticket.audit.entity.AuditLogEntity;
import com.acme.support.ticket.audit.service.AuditLogService;
import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.security.LoginUserPrincipal;
import com.acme.support.ticket.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计日志切面。
 */
@Aspect
@Component
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(com.acme.support.ticket.audit.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuditLog auditLog = method.getAnnotation(AuditLog.class);

        AuditLogEntity entity = new AuditLogEntity();
        LoginUserPrincipal principal = SecurityUtils.getLoginUser();
        entity.setOperatorUserId(principal != null ? principal.userId() : null);
        entity.setOperatorName(principal != null ? principal.username() : "anonymous");
        entity.setModuleName(auditLog.module());
        entity.setOperationType(auditLog.operation());
        entity.setOperationDescription(resolveDescription(auditLog, result));
        entity.setBusinessId(resolveBusinessId(joinPoint.getArgs(), result));

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entity.setRequestPath(request.getRequestURI());
            entity.setRequestIp(request.getRemoteAddr());
        }

        auditLogService.save(entity);
        return result;
    }

    private String resolveDescription(AuditLog auditLog, Object result) {
        if (auditLog.description() != null && !auditLog.description().isBlank()) {
            return auditLog.description();
        }

        if (result instanceof ApiResponse<?> response) {
            return response.message();
        }

        return auditLog.operation();
    }

    private Long resolveBusinessId(Object[] args, Object result) {
        for (Object arg : args) {
            if (arg instanceof Long value) {
                return value;
            }
        }

        if (result instanceof ApiResponse<?> response && response.data() instanceof Long value) {
            return value;
        }

        return null;
    }
}
