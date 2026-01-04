package com.atablood.iWindoor_api.service;

import com.atablood.iWindoor_api.dto.ProjectCostSummaryDTO;

public interface PricingService {
    // Bir projenin toplam tutarını hesaplar ve kaydeder
    void calculateProjectPrice(Long projectId);
    ProjectCostSummaryDTO getProjectCostSummary(Long projectId);
}