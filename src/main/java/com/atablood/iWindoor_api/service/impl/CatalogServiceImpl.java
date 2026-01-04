package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.Glass;
import com.atablood.iWindoor_api.entity.Profile;
import com.atablood.iWindoor_api.entity.ProfileType;
import com.atablood.iWindoor_api.entity.Series;
import com.atablood.iWindoor_api.repository.GlassRepository;
import com.atablood.iWindoor_api.repository.ProfileRepository;
import com.atablood.iWindoor_api.repository.SeriesRepository;
import com.atablood.iWindoor_api.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final SeriesRepository seriesRepository;
    private final ProfileRepository profileRepository;
    private final GlassRepository glassRepository;

    @Override
    public Series createSeries(Series series) {
        // İleride buraya kontrol ekleyebilirsin: "Aynı isimde seri var mı?"
        return seriesRepository.save(series);
    }

    @Override
    public List<Series> getAllSeries() {
        return seriesRepository.findAll();
    }

    @Override
    public Profile createProfile(Profile profile) {
        // Profilin serisi veritabanında var mı kontrolü yapılabilir
        return profileRepository.save(profile);
    }

    @Override
    public List<Profile> getProfilesBySeries(Long seriesId) {
        return profileRepository.findBySeriesId(seriesId);
    }

    @Override
    public Glass createGlass(Glass glass) {
        return glassRepository.save(glass);
    }

    @Override
    public List<Glass> getAllGlasses() {
        return glassRepository.findAll();
    }

    // Service Impl
    @Override
    public List<Profile> getProfilesByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    // Price Impl
    @Override
    public void updateProfilePrice(Long id, java.math.BigDecimal newPrice) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil bulunamadı"));
        profile.setPricePerMeter(newPrice);
        profileRepository.save(profile);
    }
}

