package com.volleyball.finder.aop;


import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component("customRateLimiterAspect")
@RequiredArgsConstructor
public class CustomRateLimiterAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("execution(* com.volleyball.finder.controller..*(..))")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("globalLimiter");

        if (rateLimiter.acquirePermission()) {
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for method: {}", joinPoint.getSignature());
            throw new ApiException(ErrorCode.FORBIDDEN, "請求過於頻繁，請稍後再試");
        }
    }
}