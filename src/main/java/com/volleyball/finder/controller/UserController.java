package com.volleyball.finder.controller;

import com.volleyball.finder.dto.UserUpdateDto;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        return ResponseEntity.of(
                java.util.Optional.ofNullable(userService.findById(id))
        );
    }

    /* 取得當前登入使用者 ------------------------------------------------ */

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        log.info("GET /api/users/me");
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    /* 新增使用者 -------------------------------------------------------- */

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        log.info("POST /api/users");
        return ResponseEntity.ok(userService.createUser(user));
    }

    /* 更新使用者 -------------------------------------------------------- */

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("PUT /api/users/{}", id);
        return ResponseEntity.ok(userService.updateUser(id, userUpdateDto));
    }

    /* 刪除使用者 -------------------------------------------------------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);
        userService.delete(id);
        return ResponseEntity.ok().build();
    }

    /* 登出 ------------------------------------------------------------ */

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        CookieUtils.clear("token", response);
        return ResponseEntity
                .status(HttpStatus.FOUND)        // 302
                .header(HttpHeaders.LOCATION, frontendUrl)
                .build();
    }
}