package com.volleyball.finder.service.impl;

import com.volleyball.finder.entity.User;
import com.volleyball.finder.enums.Role;
import com.volleyball.finder.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        log.info("Loading LINE user, token: {}", userRequest.getAccessToken().getTokenValue());

        // 讓父類去呼叫 LINE profile API；這一步可能拋出 OAuth2AuthenticationException
        OAuth2User oauth2User = super.loadUser(userRequest);

        return toAppUser(oauth2User);   // 我們自己的處理
    }

    // -------- private helpers --------

    /**
     * 把 LINE OAuth2User 轉成我們系統認可的 OAuth2User（攜帶 JWT 之後要的資訊）
     */
    private OAuth2User toAppUser(OAuth2User oauth2User) {

        Map<String, Object> raw = oauth2User.getAttributes();

        // 1. 取出 LINE 的 userId，若缺少就直接拋例外，由 Spring Security 統一處理
        String lineId = Optional.ofNullable((String) raw.get("userId"))
                .filter(id -> !id.isBlank())
                .orElseThrow(() ->
                        new OAuth2AuthenticationException("line_id_not_found"));

        // 2. 若 DB 無此人就創建
        User user = userService.findByLineId(lineId)
                .orElseGet(() -> registerNewUser(raw));

        // 3. 把 User 放進 attributes 回傳
        Map<String, Object> merged = new HashMap<>(raw);
        merged.put("user", user);
        merged.put("lineId", lineId);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                merged,
                "userId"                     // 讓 Spring Security 用 userId 當 nameAttributeKey
        );
    }

    /**
     * 建立新使用者，只做最小必要欄位
     */
    private User registerNewUser(Map<String, Object> attributes) {
        log.info("Registering new user for LINE ID {}", attributes.get("userId"));

        int randomSeed = ThreadLocalRandom.current().nextInt(1, 10001); // 1～10000 的亂數
        String avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=" + randomSeed;

        User user = new User();
        user.setLineId((String) attributes.get("userId"));
        user.setNickname((String) attributes.get("displayName"));
        user.setRole(Role.USER);
        user.setAvatar(avatarUrl);

        return userService.createUser(user);
    }
}