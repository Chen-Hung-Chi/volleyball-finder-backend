package com.volleyball.finder.error;

public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code) {
        super(code.defaultMsg());
        this.code = code;
    }

    public ApiException(ErrorCode code, String customMsg) {
        super(customMsg);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}