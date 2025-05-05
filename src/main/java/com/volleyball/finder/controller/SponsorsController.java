package com.volleyball.finder.controller;


import com.volleyball.finder.dto.SponsorResponse;
import com.volleyball.finder.dto.SponsorUpdateRequest;
import com.volleyball.finder.entity.Sponsors;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.service.SponsorService;
import com.volleyball.finder.service.UserService;
import com.volleyball.finder.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
@Slf4j
public class SponsorsController {

    private final UserService userService;
    private final SponsorService sponsorService;

    @PostMapping
    public ResponseEntity<Void> createSponsor(@Valid @RequestBody Sponsors sponsors) {
        long currentUserId = SecurityUtils.getCurrentUserId(userService);
        if (sponsorService.findByUserId(currentUserId).isPresent()) {
            throw new ApiException(ErrorCode.SPONSOR_PROFILE_ALREADY_EXISTS);
        }

        sponsors.setUserId(currentUserId);
        log.info("User {} is creating sponsor.", currentUserId);
        sponsorService.createSponsor(sponsors);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<SponsorResponse>> getAllSponsors() {
        List<SponsorResponse> sponsors = sponsorService.getAllSponsors();

        if (sponsors.isEmpty()) {
            log.info("No sponsors found.");
        } else {
            log.info("Retrieved {} sponsors.", sponsors.size());
        }

        return ResponseEntity.ok(sponsors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SponsorResponse> getSponsorById(@PathVariable Long id) {
        return sponsorService.getSponsorById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ApiException(ErrorCode.SPONSOR_NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SponsorResponse> updateSponsor(@PathVariable Long id,
                                                         @RequestBody SponsorUpdateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId(userService);

        // 查詢該 sponsor 是否屬於當前使用者
        sponsorService.findByUserId(currentUserId).ifPresentOrElse(sponsor -> {
            if (!sponsor.getId().equals(id)) {
                throw new ApiException(ErrorCode.INVALID_REQUEST);
            }
        }, () -> {
            throw new ApiException(ErrorCode.SPONSOR_NOT_FOUND);
        });

        SponsorResponse updated = sponsorService.updateSponsor(id, request);
        return ResponseEntity.ok(updated);
    }

}


