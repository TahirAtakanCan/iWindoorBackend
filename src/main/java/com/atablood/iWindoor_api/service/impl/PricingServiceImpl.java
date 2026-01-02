package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.Project;
import com.atablood.iWindoor_api.entity.WindowNode;
import com.atablood.iWindoor_api.entity.WindowUnit;
import com.atablood.iWindoor_api.repository.ProjectRepository;
import com.atablood.iWindoor_api.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public void calculateProjectPrice(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        double totalProjectPrice = 0.0;

        // Projedeki her pencereyi gez
        for (WindowUnit unit : project.getWindowUnits()) {
            if (unit.getRootNode() != null) {
                // Recursive hesaplamayı başlat
                totalProjectPrice += calculateNodeCost(unit.getRootNode());
            }
        }

        project.setTotalPrice(BigDecimal.valueOf(totalProjectPrice));
        projectRepository.save(project);
    }

    // RECURSIVE HESAPLAMA FONKSİYONU
    private double calculateNodeCost(WindowNode node) {
        double nodeCost = 0.0;

        // Konsola Bilgi Basalım (Debug)
        System.out.println("--- Hesaplanan Parça ID: " + node.getId() + " [" + node.getNodeType() + "] ---");

        // Ölçüleri Metreye Çevir
        double widthM = node.getWidth() / 1000.0;
        double heightM = node.getHeight() / 1000.0;

        // 1. PROFİL MALİYETİ
        if (node.getProfile() != null) {
            double lengthM = 0.0;
            double price = node.getProfile().getPricePerMeter().doubleValue();

            if (node.getNodeType().name().equals("FRAME") || node.getNodeType().name().equals("SASH")) {
                lengthM = (widthM + heightM) * 2;
            } else if (node.getNodeType().name().equals("MULLION_VERTICAL")) {
                lengthM = heightM;
            } else if (node.getNodeType().name().equals("MULLION_HORIZONTAL")) {
                lengthM = widthM;
            }

            double cost = lengthM * price;
            nodeCost += cost;

            System.out.println("   -> Profil Bulundu: " + node.getProfile().getName());
            System.out.println("   -> Metraj: " + lengthM + "m x Fiyat: " + price + " TL = " + cost);
        } else {
            System.out.println("   -> Bu parçada Profil YOK (Maliyet 0)");
        }

        // 3. ÇOCUKLARIN MALİYETİNİ EKLE
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (WindowNode child : node.getChildren()) {
                nodeCost += calculateNodeCost(child);
            }
        }

        System.out.println("   -> Parça Toplamı: " + nodeCost);
        return nodeCost;
    }
}