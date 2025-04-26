package com.volleyball.finder.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleBiz(ApiException ex) {
        ErrorCode ec = ex.code();
        return ResponseEntity.status(ec.status())
                .body(new ApiError(ec, ex.getMessage()));
    }

    /** 所有沒攔到的例外都走這裡，避免 stack trace 直接丟前端 */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleOther(Throwable ex) {
        log.error("Unhandled exception", ex);
        ErrorCode ec = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(ec.status())
                .body(new ApiError(ec));
    }
}