package com.volleyball.finder.security;

import com.volleyball.finder.entity.User;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.service.JwtService;
import com.volleyball.finder.util.CookieUtils;
import com.volleyball.finder.util.RedirectUtils;
import com.volleyball.finder.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String TOKEN_COOKIE = "token";
    private static final Duration ONE_WEEK = Duration.ofDays(7);

    private final JwtService jwtService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Authentication auth) throws IOException {

        if (resp.isCommitted()) {
            log.debug("Response already committed, aborting.");
            return;
        }

        var userOpt = SecurityUtils.extractUser((OAuth2User) auth.getPrincipal());

        if (userOpt.isEmpty()) {                // 找不到 User → 直接回前端錯誤
            RedirectUtils.redirectError(resp, frontendUrl, ErrorCode.USER_NOT_FOUND);
            return;
        }

        User user = userOpt.get();

        log.info("OAuth2 success – LINE: {}, nickname: {}", user.getLineId(), user.getNickname());

        // ① 產生 JWT，寫入安全 Cookie
        String jwt = jwtService.generateToken(user);
        CookieUtils.addCookie(resp, TOKEN_COOKIE, jwt, ONE_WEEK);
        // ② 清掉 Spring Security 的 session attr
        clearAuthenticationAttributes(req);

        // ③ 回前端
        getRedirectStrategy().sendRedirect(req, resp, frontendUrl);
    }

}