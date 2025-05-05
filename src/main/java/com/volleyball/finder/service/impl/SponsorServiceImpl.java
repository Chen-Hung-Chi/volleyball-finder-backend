package com.volleyball.finder.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volleyball.finder.dto.SponsorResponse;
import com.volleyball.finder.dto.SponsorUpdateRequest;
import com.volleyball.finder.entity.Sponsors;
import com.volleyball.finder.error.ApiException;
import com.volleyball.finder.error.ErrorCode;
import com.volleyball.finder.mapper.SponsorMapper;
import com.volleyball.finder.service.SponsorService;
import com.volleyball.finder.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SponsorServiceImpl implements SponsorService {

    private final SponsorMapper sponsorMapper;
    private final AESUtil aesUtil;

    @Override
    public void createSponsor(Sponsors sponsors) {
        String secret = sponsors.getLinePayChannelSecret();
        if (secret != null && !secret.isBlank()) {
            sponsors.setLinePayChannelSecret(aesUtil.encrypt(secret));
        }

        int inserted = sponsorMapper.insert(sponsors);
        if (inserted <= 0) {
            throw new ApiException(ErrorCode.SPONSOR_CREATE_FAILED);
        }
    }

    @Override
    public List<SponsorResponse> getAllSponsors() {
        return sponsorMapper.selectSponsorResponses();
    }

    @Override
    public Optional<SponsorResponse> getSponsorById(Long id) {
        return Optional.ofNullable(sponsorMapper.selectSponsorResponseById(id));
    }

    @Override
    public Optional<Sponsors> findByUserId(Long userId) {
        QueryWrapper<Sponsors> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        Sponsors sponsors = sponsorMapper.selectOne(wrapper);
        return Optional.ofNullable(sponsors);
    }

    @Override
    public SponsorResponse updateSponsor(Long id, SponsorUpdateRequest request) {
        Sponsors sponsors = new Sponsors();
        sponsors.setId(id);

        // 拷貝欄位（忽略 null 欄位，避免覆蓋掉 DB 中原本有值的欄位）
        BeanUtil.copyProperties(request, sponsors, CopyOptions.create().ignoreNullValue());

        int updated = sponsorMapper.updateById(sponsors);
        if (updated <= 0) {
            throw new ApiException(ErrorCode.SPONSOR_UPDATE_FAILED);
        }

        return sponsorMapper.selectSponsorResponseById(id);
    }

}