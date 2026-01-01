package com.atablood.iWindoor_api.service;

import com.atablood.iWindoor_api.entity.Glass;
import com.atablood.iWindoor_api.entity.Profile;
import com.atablood.iWindoor_api.entity.Series;
import java.util.List;

public interface CatalogService {
    // Seri İşlemleri
    Series createSeries(Series series);
    List<Series> getAllSeries();

    // Profil İşlemleri
    Profile createProfile(Profile profile);
    List<Profile> getProfilesBySeries(Long seriesId);

    // Cam İşlemleri
    Glass createGlass(Glass glass);
    List<Glass> getAllGlasses();
}
