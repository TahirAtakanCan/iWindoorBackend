package com.atablood.iWindoor_api.service;

import com.atablood.iWindoor_api.entity.Project;

public interface PricingService {
    // Bir projenin toplam tutarını hesaplar ve kaydeder
    void calculateProjectPrice(Long projectId);
}