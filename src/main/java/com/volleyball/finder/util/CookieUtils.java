package com.volleyball.finder.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Optional;

public final class CookieUtils {

    private CookieUtils() {
    }

    /**
     * 建立 HttpOnly + SameSite=Lax 的 Cookie
     */
    public static Cookie build(String name, String value, Duration maxAge, boolean secure) {
        Cookie c = new Cookie(name, value);
        c.setHttpOnly(true);
        c.setSecure(secure);
        c.setPath("/");
        c.setMaxAge((int) maxAge.toSeconds());
        c.setAttribute("SameSite", "Lax");   // 降低 CSRF 風險
        return c;
    }

    /**
     * 立即失效並寫回前端（用於登出/清除）
     */
    public static void clear(String name, HttpServletResponse resp) {
        Cookie c = new Cookie(name, "");
        c.setPath("/");
        c.setMaxAge(0);
        resp.addCookie(c);
    }

    /**
     * 從 request 取得指定 Cookie 值
     */
    public static Optional<String> get(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return Optional.empty();
        for (Cookie c : req.getCookies()) {
            if (name.equals(c.getName())) return Optional.ofNullable(c.getValue());
        }
        return Optional.empty();
    }
}