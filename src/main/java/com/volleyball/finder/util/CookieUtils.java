package com.volleyball.finder.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtils {

    private static String activeProfile;

    @Value("${spring.profiles.active:local}")
    public void setActiveProfile(String profile) {
        CookieUtils.activeProfile = profile;
    }

    private static boolean isProd() {
        return "prod".equalsIgnoreCase(activeProfile);
    }

    /**
     * 設定安全的 JWT Cookie（含 HttpOnly, Secure, SameSite）
     */
    public static void addCookie(HttpServletResponse response, String name, String value, Duration maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // SameSite=None 也需要 Secure
        cookie.setMaxAge((int) maxAge.toSeconds());

        // Java Servlet 標準沒有支援 SameSite，要用 header 補上
        response.addCookie(cookie);
        response.addHeader("Set-Cookie", String.format(
                "%s=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=%s",
                name,
                value,
                maxAge.toSeconds(),
                isProd() ? "Lax" : "None"
        ));
    }

    /**
     * 清除 Cookie（設 Max-Age=0）
     */
    public static void clear(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);
        response.addHeader("Set-Cookie", String.format(
                "%s=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=%s",
                name,
                isProd() ? "Lax" : "None"
        ));
    }

    /**
     * 從 Request 中讀取指定 Cookie 值
     */
    public static Optional<String> get(HttpServletRequest request, String name) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}