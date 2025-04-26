package com.volleyball.finder.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 1XXX – 通用
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器忙碌，請稍後再試"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "無效的請求"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "禁止訪問"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "資源不存在"),

    // 2XXX – 使用者
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "用戶不存在"),
    USER_EXISTS(HttpStatus.CONFLICT, "用戶已存在"),
    NICKNAME_EXISTS(HttpStatus.CONFLICT, "暱稱已存在"),

    // 3XXX – 活動
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "活動不存在"),
    ACTIVITY_NOT_JOINED(HttpStatus.BAD_REQUEST, "未參加此活動"),
    ACTIVITY_LEAVED(HttpStatus.CONFLICT, "你已取消報名此活動"),
    ACTIVITY_FULL(HttpStatus.CONFLICT, "活動已額滿"),
    ACTIVITY_JOINED(HttpStatus.CONFLICT, "已參加此活動"),
    ACTIVITY_WAIT_30M(HttpStatus.CONFLICT, "退出後需等待 30 分鐘才能重新加入"),
    ACTIVITY_FEMALE_PRIORITY(HttpStatus.CONFLICT, "目前為女生優先，待女生名額額滿後才能報名"),
    ACTIVITY_MALE_FULL(HttpStatus.CONFLICT, "男生名額已滿，請等待候補或選擇其他場次"),
    ACTIVITY_FEMALE_FULL(HttpStatus.CONFLICT, "女生名額已滿，請等待候補或選擇其他場次"),
    ACTIVITY_GENDER_QUOTA_FULL(HttpStatus.CONFLICT, "該性別名額已額滿，無法報名此活動");    // … 其它錯誤 …
    ;

    private final HttpStatus status;
    private final String defaultMsg;

    ErrorCode(HttpStatus status, String defaultMsg) {
        this.status = status;
        this.defaultMsg = defaultMsg;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMsg() {
        return defaultMsg;
    }
}