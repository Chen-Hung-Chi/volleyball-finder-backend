package com.volleyball.finder.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.security.CustomUserDetails;
import com.volleyball.finder.service.UserService;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class SecurityUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * new ObjectMapper();
     * 嘗試取得目前登入者的 LINE ID，不存在時回傳 null
     */
    public String getCurrentLineId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof CustomUserDetails customUser) {
                        return customUser.getLineId();
                    } else if (principal instanceof OAuth2User oauth2User) {
                        Object lineId = oauth2User.getAttributes().get("userId");
                        return lineId instanceof String ? (String) lineId : null;
                    } else {
                        return null;
                    }
                })
                .orElse(null);
    }

    /**
     * 從目前登入者資訊中找出對應的 User ID，不存在會丟錯
     */
    public Long getCurrentUserId(UserService userService) {
        String lineId = getCurrentLineId();

        return Optional.ofNullable(lineId)
                .flatMap(userService::findByLineId)
                .map(User::getId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "尚未登入或找不到用戶資料"));
    }

    public Optional<User> extractUser(OAuth2User oauth) {
        return Optional.ofNullable(oauth.getAttribute("user"))
                .flatMap(attr -> switch (attr) {
                    case User u -> Optional.of(u);
                    case Map<?, ?> map -> mapToUser(map);
                    default -> Optional.empty();
                });
    }

    private Optional<User> mapToUser(Map<?, ?> map) {
        try {
            return Optional.of(MAPPER.convertValue(map, User.class));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}