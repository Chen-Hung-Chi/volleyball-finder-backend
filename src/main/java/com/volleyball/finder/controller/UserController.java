package com.volleyball.finder.controller;

import com.volleyball.finder.dto.*;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.security.CustomUserDetails;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.CookieUtils;
import com.volleyball.finder.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /* 取得指定使用者 ---------------------------------------------------- */

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.of(userService.getUserResponseById(id));
    }

    /* 取得當前登入使用者 ------------------------------------------------ */

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    /* 更新使用者 -------------------------------------------------------- */

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        if (!user.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var updated = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(updated);
    }

    /* 刪除使用者 -------------------------------------------------------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    /* 登出 ------------------------------------------------------------ */

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response) {
        // 清掉 token cookie
        CookieUtils.clear("token", response);

        // 重要！SecurityContext清掉
        SecurityContextHolder.clearContext();

        // Optional: 如果你有session，要讓session失效
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok().build();
    }

    /* 檢查暱稱是否已存在 -------------------------------------------------------- */

    @GetMapping("/check-nickname")
    public ResponseEntity<BooleanResponse> checkNicknameExists(@RequestParam String nickname) {
        boolean available = !userService.isNicknameTaken(nickname);
        return ResponseEntity.ok(new BooleanResponse(available));
    }

    /* 更新FCM token -------------------------------------------------------- */

    @PatchMapping("/fcm-token")
    public String updateFcmToken(@RequestBody UpdateFcmTokenRequest request) {
        Long userId = SecurityUtils.getCurrentUserId(userService);
        userService.updateFcmToken(userId, request.getFcmToken());
        return "FCM Token updated successfully.";
    }

}