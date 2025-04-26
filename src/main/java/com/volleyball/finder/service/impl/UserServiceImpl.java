package com.volleyball.finder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volleyball.finder.dto.UserUpdateDto;
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
    public User updateUser(Long id, UserUpdateDto dto) {
        if (dto == null || id == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "用戶或用戶 ID 不可為空");
        }

        log.info("更新用戶: {}", dto);

        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "找不到用戶 ID: " + id);
        }

        // 將 dto 資料複製到 existingUser（只會覆蓋非 null 欄位）
        BeanUtils.copyProperties(dto, existingUser);

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
}
