package com.volleyball.finder.service;

import com.volleyball.finder.dto.SponsorResponse;
import com.volleyball.finder.dto.SponsorUpdateRequest;
import com.volleyball.finder.entity.Sponsors;

import java.util.List;
import java.util.Optional;

public interface SponsorService {
    void createSponsor(Sponsors sponsors);

    List<SponsorResponse> getAllSponsors();

    Optional<SponsorResponse> getSponsorById(Long id);

    Optional<Sponsors> findByUserId(Long userId);

    SponsorResponse updateSponsor(Long id, SponsorUpdateRequest request);

}