package com.billsplit.aop;

import com.billsplit.entity.ActivityLog;
import com.billsplit.entity.User;
import com.billsplit.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Intercepts all @LogActivity methods, times execution,
 * and persists an audit row — zero logging code in services.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {
    private final ActivityLogRepository activityLogRepository;

    @Around("@annotation(com.billsplit.aop.LogActivity)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        var method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String action = method.getAnnotation(LogActivity.class).action();

        String username = "anonymous";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User u) {
            username = u.getEmail();
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            activityLogRepository.save(ActivityLog.builder()
                    .username(username)
                    .action(action)
                    .details(method.getName())
                    .executionTimeMs(System.currentTimeMillis() - start)
                    .build());
        }
        return result;
    }
}
