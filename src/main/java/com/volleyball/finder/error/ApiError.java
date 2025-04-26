package com.volleyball.finder.error;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp) {

    public ApiError(ErrorCode ec, String msg) {
        this(ec.name(), msg, Instant.now());
    }

    public ApiError(ErrorCode ec) {
        this(ec, ec.defaultMsg());
    }
}