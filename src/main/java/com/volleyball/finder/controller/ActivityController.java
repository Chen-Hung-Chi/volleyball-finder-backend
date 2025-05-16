package com.volleyball.finder.controller;

import com.volleyball.finder.dto.*;
import com.volleyball.finder.entity.Activity;
import com.volleyball.finder.entity.User;
import com.volleyball.finder.helper.ActivityAuthHelper;
import com.volleyball.finder.security.CustomUserDetails;
import com.volleyball.finder.service.ActivityService;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;
    private final UserService userService;
    private final ActivityAuthHelper activityAuthHelper;

    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(activityService.findAll(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Activity>> getUserActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(activityService.findByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<Activity>> getMyActivities() {
        Long userId = SecurityUtils.getCurrentUserId(userService);
        return ResponseEntity.ok(activityService.findByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @Valid @RequestBody Activity activity) {
        activity.setCreatedBy(userDetails.getId());
        var created = activityService.create(activity);
        var location = URI.create("/api/activities/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivity(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(activityService.findById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityUpdateRequest activityUpdateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails // 可選：判斷權限
    ) {
        Activity updated = activityService.update(id, activityUpdateRequest, userDetails.getId());
        if (updated == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.ok(updated); // 200 + 更新後內容
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        // 不是superUser的不能刪除
        if (id != 1L) {
            return ResponseEntity.status(401).build();
        }
        activityService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinActivity(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        activityService.joinActivity(id, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveActivity(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId(userService);
        activityService.leaveActivity(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ActivityParticipantDto>> getActivityParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityParticipants(id));
    }

    @GetMapping("/search")
    public PageResponse<Activity> search(ActivitySearchRequest request) {
        return activityService.search(request);
    }

    @GetMapping("/{activityId}/users")
    public ResponseEntity<List<UserPrivateResponse>> getUserProfiles(
            @PathVariable Long activityId,
            @RequestParam List<Long> ids,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        // 權限判斷：只有隊長能查
        boolean isCaptain = activityAuthHelper.isCaptain(activityId, currentUser.getId());
        if (!isCaptain) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // 一次查多筆 user 資料
        List<UserPrivateResponse> users = userService.getUserPrivateResponseByIds(ids);
        return ResponseEntity.ok(users);
    }
}