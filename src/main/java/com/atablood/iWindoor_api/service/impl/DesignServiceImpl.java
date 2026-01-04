package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.Glass;
import com.atablood.iWindoor_api.entity.NodeType;
import com.atablood.iWindoor_api.entity.Profile;
import com.atablood.iWindoor_api.entity.WindowNode;
import com.atablood.iWindoor_api.repository.GlassRepository;
import com.atablood.iWindoor_api.repository.ProfileRepository;
import com.atablood.iWindoor_api.repository.WindowNodeRepository;
import com.atablood.iWindoor_api.service.DesignService;
import com.atablood.iWindoor_api.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignServiceImpl implements DesignService {

    private final WindowNodeRepository windowNodeRepository;
    private final ProfileRepository profileRepository;
    private final GlassRepository glassRepository;

    // İleride otomatik hesaplama tetiklemek istersen burada dursun
    private final PricingService pricingService;

    @Override
    @Transactional
    public WindowNode splitNode(Long nodeId, boolean isVertical) {

        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        // Düğümü konteyner (kayıt) haline getir
        node.setNodeType(
                isVertical ? NodeType.MULLION_VERTICAL : NodeType.MULLION_HORIZONTAL
        );

        // Eğer bölünen parça daha önce bir profil veya cama sahipse, bunları temizle
        node.setProfile(null);
        node.setGlass(null);
        node.setStoredPrice(null); // Fiyat bilgisini de temizle

        // Eşit bölme (ileride profil kalınlığı düşülebilir)
        double newWidth = isVertical ? node.getWidth() / 2 : node.getWidth();
        double newHeight = isVertical ? node.getHeight() : node.getHeight() / 2;

        // Sol / Üst çocuk oluştur
        createChildNode(node, newWidth, newHeight, 0);

        // Sağ / Alt çocuk oluştur
        createChildNode(node, newWidth, newHeight, 1);

        return windowNodeRepository.save(node);
    }

    private void createChildNode(WindowNode parent, double w, double h, int order) {
        WindowNode child = new WindowNode();
        child.setNodeType(NodeType.EMPTY);
        child.setWidth(w);
        child.setHeight(h);
        child.setItemOrder(order);
        child.setParent(parent);
        parent.getChildren().add(child);
    }

    @Override
    @Transactional
    public WindowNode updateNodeType(Long nodeId, String nodeTypeStr) {

        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        try {
            NodeType newType = NodeType.valueOf(nodeTypeStr);

            if (node.getNodeType() == NodeType.FRAME ||
                    node.getNodeType() == NodeType.MULLION_VERTICAL ||
                    node.getNodeType() == NodeType.MULLION_HORIZONTAL) {
                throw new RuntimeException("Taşıyıcı parçalar değiştirilemez!");
            }

            node.setNodeType(newType);
            return windowNodeRepository.save(node);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz parça tipi: " + nodeTypeStr);
        }
    }

    @Override
    @Transactional
    public WindowNode assignMaterial(Long nodeId, Long materialId, String materialType) {

        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        if ("PROFILE".equalsIgnoreCase(materialType)) {

            Profile profile = profileRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Profil bulunamadı"));

            node.setProfile(profile);
            node.setGlass(null);

            // --- YENİ: Fiyatı Sabitle (Snapshot) ---
            // Profil atandığı andaki fiyatı node üzerine yazıyoruz.
            node.setStoredPrice(profile.getPricePerMeter());

        } else if ("GLASS".equalsIgnoreCase(materialType)) {

            Glass glass = glassRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Cam bulunamadı"));

            node.setGlass(glass);
            node.setProfile(null);

            // --- YENİ: Fiyatı Sabitle (Snapshot) ---
            node.setStoredPrice(glass.getPricePerSquareMeter());

        } else {
            throw new RuntimeException("Geçersiz materialType: " + materialType);
        }

        return windowNodeRepository.save(node);
    }
}