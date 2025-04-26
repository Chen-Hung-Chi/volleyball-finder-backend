package com.volleyball.finder.aop;

import com.volleyball.finder.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void apiMethods() {
    }

    @Around("apiMethods()")
    public Object logApiCall(ProceedingJoinPoint pjp) throws Throwable {
        var method = request.getMethod();
        var uri = request.getRequestURI();
        var query = request.getQueryString();
        var ip = getClientIp();
        var contentType = request.getContentType();
        var lineId = SecurityUtils.getCurrentLineId();

        lineId = (lineId == null || lineId.isBlank()) ? "Anonymous" : lineId;

        var argsStr = (contentType != null && contentType.startsWith("multipart/"))
                ? "File Upload"
                : Arrays.toString(pjp.getArgs());

        // 執行 Controller
        Object result = pjp.proceed();

        var status = response.getStatus(); // 取得 Response Status Code

        log.info("[{}] {}{} | IP={} | LineId={} | Status={} | Args={}",
                method,
                uri,
                query != null ? "?" + query : "",
                ip,
                lineId,
                status,
                argsStr
        );

        return result;
    }

    private String getClientIp() {
        var forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0]
                : request.getRemoteAddr();
    }
}