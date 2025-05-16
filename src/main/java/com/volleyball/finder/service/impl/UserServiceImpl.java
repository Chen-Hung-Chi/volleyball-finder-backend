package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.volleyball.finder.dto.UserPrivateResponse;
import com.volleyball.finder.dto.UserResponse;
import com.volleyball.finder.dto.UserUpdateRequest;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.mapper.UserMapper;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 使用者相關邏輯實作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public User findById(Long id) {
        log.info("依 ID 查找用戶: {}", id);
        return Optional.ofNullable(userMapper.selectById(id))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Optional<User> findByLineId(String lineId) {
        if (!StringUtils.hasText(lineId)) {
            return Optional.empty();
        }

        log.info("依 LINE ID 查找用戶: {}", lineId);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getLineId, lineId)
                        .last("LIMIT 1")
        );

        return Optional.ofNullable(user);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("創建新用戶: {}", user);
        userMapper.insert(user);
        log.info("已創建用戶，ID: {}", user.getId());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateRequest dto) {
        Objects.requireNonNull(id, "用戶 ID 不可為空");
        Objects.requireNonNull(dto, "用戶資料不可為空");

        log.info("更新用戶: {}", dto);

        var existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "找不到用戶 ID: " + id);
        }

        // 實名制驗證成功
        if (StringUtils.hasText(dto.getRealName()) && StringUtils.hasText(dto.getPhone())) {
            existingUser.setRealName(dto.getRealName());
            existingUser.setPhone(dto.getPhone());
            existingUser.setIsVerified(Boolean.TRUE);
        }

        // 只會覆蓋非 null 欄位，注意不能讓上面已特別處理的欄位被覆蓋
        BeanUtils.copyProperties(dto, existingUser, "realName", "phone", "isVerified");

        int result = userMapper.updateById(existingUser);
        if (result == 0) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "更新失敗，可能用戶已不存在");
        }

        log.info("成功更新用戶 ID: {}", existingUser.getId());
        return existingUser;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("刪除用戶 ID: {}", id);
        int result = userMapper.deleteById(id);
        if (result == 0) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "用戶刪除失敗，ID: " + id);
        }
        log.info("成功刪除用戶 ID: {}", id);
    }

    @Override
    public User getCurrentUser() {
        String lineId = SecurityUtils.getCurrentLineId();
        log.info("目前登入者的 lineId: {}", lineId);

        Optional<User> user = findByLineId(lineId);
        if (user.isEmpty() && !StringUtils.hasText(lineId)) {
            log.warn("查不到使用者，lineId 可能加密不一致: {}", lineId);
        }
        return Optional.ofNullable(lineId)
                .flatMap(this::findByLineId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "尚未登入或找不到用戶資料"));
    }

    @Override
    public boolean isNicknameTaken(String nickname) {
        return userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getNickname, nickname)
                        .last("LIMIT 1")
        ) > 0;
    }

    @Override
    public String getFcmToken(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getFcmToken() : null;
    }

    @Override
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        userMapper.update(null, new UpdateWrapper<User>()
                .eq("id", userId)
                .set("fcm_token", fcmToken));
    }

    @Override
    public Optional<UserResponse> getUserResponseById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Optional.empty();

        UserResponse dto = new UserResponse();
        BeanUtils.copyProperties(user, dto);
        return Optional.of(dto);
    }

    @Override
    public List<UserPrivateResponse> getUserPrivateResponseByIds(List<Long> ids) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("id", "real_name", "phone");  // 指定欄位
        wrapper.in("id", ids);

        // 查出 User，再轉成 UserPrivateResponse
        List<User> users = userMapper.selectList(wrapper);

        return users.stream()
                .map(user -> new UserPrivateResponse(user.getId(), user.getRealName(), user.getPhone()))
                .toList();
    }
}
