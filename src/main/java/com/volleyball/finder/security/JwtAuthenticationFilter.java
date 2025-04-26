package com.volleyball.finder.security;

import com.volleyball.finder.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_COOKIE = "token";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws ServletException, IOException {

        // 若已經存在認證資訊，直接放行
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        var tokenOpt = resolveToken(req);

        try {
            tokenOpt
                    .filter(jwtService::validateToken)   // 僅當 token 有效才進一步處理
                    .ifPresent(token -> {
                        var principal = new CustomUserDetails(
                                jwtService.getClaim(token, "id", Long.class),
                                jwtService.getClaim(token, "nickname", String.class),
                                jwtService.getClaim(token, "lineId", String.class),
                                Collections.emptyList()
                        );

                        var auth = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        } catch (ExpiredJwtException e) {
            log.info("JWT 已過期: {}", e.getMessage());
            clearTokenCookie(res);                     // 選擇性：清除過期 cookie
        } catch (JwtException e) {
            log.warn("JWT 無效: {}", e.getMessage());
        }

        chain.doFilter(req, res);                      // 統一放行
    }

    private Optional<String> resolveToken(HttpServletRequest req) {
        return Optional.ofNullable(req.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(c -> TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void clearTokenCookie(HttpServletResponse res) {
        Cookie c = new Cookie(TOKEN_COOKIE, "");
        c.setMaxAge(0);
        c.setPath("/");
        res.addCookie(c);
    }
}