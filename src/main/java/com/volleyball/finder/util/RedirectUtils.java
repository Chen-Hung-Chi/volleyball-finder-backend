package com.volleyball.finder.util;

import com.volleyball.finder.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedirectUtils {

    /**
     * 依 baseUrl 加上 queryParams 後 redirect
     */
    public static void redirect(HttpServletResponse resp,
                                String baseUrl,
                                Map<String, String> queryParams) throws IOException {

        var builder = UriComponentsBuilder.fromUriString(baseUrl);
        queryParams.forEach(builder::queryParam);

        new DefaultRedirectStrategy().sendRedirect(null, resp, builder.toUriString());
    }

    public static void redirectError(HttpServletResponse resp,
                                     String baseUrl,
                                     ErrorCode ec) throws IOException {
        redirect(resp, baseUrl, Map.of(
                "error", ec.name(),
                "msg", ec.defaultMsg()
        ));
    }
}