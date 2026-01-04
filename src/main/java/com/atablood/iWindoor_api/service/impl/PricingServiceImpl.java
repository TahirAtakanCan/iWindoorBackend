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

import com.atablood.iWindoor_api.dto.CostItemDTO;
import com.atablood.iWindoor_api.dto.ProjectCostSummaryDTO;
import com.atablood.iWindoor_api.entity.*;
import java.util.Map;
import java.util.HashMap;

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

    @Override
    public ProjectCostSummaryDTO getProjectCostSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı"));

        ProjectCostSummaryDTO summary = new ProjectCostSummaryDTO();

        // Malzemeleri toplamak için geçici haritalar (Adına göre grupla)
        Map<String, CostItemDTO> profileMap = new HashMap<>();
        Map<String, CostItemDTO> glassMap = new HashMap<>();

        // Tüm pencereleri gez
        for (WindowUnit unit : project.getWindowUnits()) {
            if (unit.getRootNode() != null) {
                collectMaterials(unit.getRootNode(), profileMap, glassMap);
            }
        }

        // Toplanan verileri rapora ekle
        profileMap.values().forEach(summary::addItem);
        glassMap.values().forEach(summary::addItem);

        // (İleride Aksesuarları da buraya ekleyeceğiz)

        return summary;
    }

    // Recursive (Özyinelemeli) Malzeme Toplayıcı
    private void collectMaterials(WindowNode node, Map<String, CostItemDTO> profileMap, Map<String, CostItemDTO> glassMap) {
        // 1. Profil Hesabı
        if (node.getProfile() != null) {
            String name = node.getProfile().getName();
            double lengthM = 0;

            // Çevre hesabı (Basitçe: En + En + Boy + Boy) -> Ama bu node sadece bir parça olabilir.
            // iWindoor mantığında Node bir "Alan"dır. Etrafındaki profiller hesaplanır.
            // Şimdilik basitleştirilmiş mantık: Eğer bu bir Kasa veya Kanat ise çevresi kadar profil gider.
            if (node.getNodeType() == NodeType.FRAME || node.getNodeType() == NodeType.SASH) {
                lengthM = (node.getWidth() * 2 + node.getHeight() * 2) / 1000.0; // mm -> m
            }
            // Kayıt (Mullion) ise sadece kendi uzunluğu
            else if (node.getNodeType() == NodeType.MULLION_VERTICAL) {
                lengthM = node.getHeight() / 1000.0;
            }
            else if (node.getNodeType() == NodeType.MULLION_HORIZONTAL) {
                lengthM = node.getWidth() / 1000.0;
            }

            if (lengthM > 0) {
                BigDecimal cost = node.getProfile().getPricePerMeter().multiply(BigDecimal.valueOf(lengthM));

                // Varsa üstüne ekle, yoksa yeni oluştur
                profileMap.merge(name,
                        new CostItemDTO(name, "Profil", lengthM, "m", cost),
                        (oldItem, newItem) -> {
                            oldItem.setQuantity(oldItem.getQuantity() + newItem.getQuantity());
                            oldItem.setPrice(oldItem.getPrice().add(newItem.getPrice()));
                            return oldItem;
                        }
                );
            }
        }

        // 2. Cam Hesabı
        if (node.getGlass() != null) {
            String name = node.getGlass().getName();
            double area = (node.getWidth() * node.getHeight()) / 1_000_000.0; // mm2 -> m2
            BigDecimal cost = node.getGlass().getPricePerSquareMeter().multiply(BigDecimal.valueOf(area));

            glassMap.merge(name,
                    new CostItemDTO(name, "Cam", area, "m2", cost),
                    (oldItem, newItem) -> {
                        oldItem.setQuantity(oldItem.getQuantity() + newItem.getQuantity());
                        oldItem.setPrice(oldItem.getPrice().add(newItem.getPrice()));
                        return oldItem;
                    }
            );
        }

        // Çocukları gez
        for (WindowNode child : node.getChildren()) {
            collectMaterials(child, profileMap, glassMap);
        }
    }
}