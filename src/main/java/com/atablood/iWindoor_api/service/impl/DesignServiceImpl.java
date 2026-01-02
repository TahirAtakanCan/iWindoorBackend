package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.Glass;
import com.atablood.iWindoor_api.entity.NodeType;
import com.atablood.iWindoor_api.entity.Profile;
import com.atablood.iWindoor_api.entity.WindowNode;
import com.atablood.iWindoor_api.repository.GlassRepository;
import com.atablood.iWindoor_api.repository.ProfileRepository;
import com.atablood.iWindoor_api.repository.WindowNodeRepository;
import com.atablood.iWindoor_api.service.DesignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignServiceImpl implements DesignService {

    private final WindowNodeRepository windowNodeRepository;
    private final ProfileRepository profileRepository;
    private final GlassRepository glassRepository;

    @Override
    @Transactional
    public WindowNode splitNode(Long nodeId, boolean isVertical) {

        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        // Düğümü konteyner (kayıt) haline getir
        node.setNodeType(
                isVertical ? NodeType.MULLION_VERTICAL : NodeType.MULLION_HORIZONTAL
        );

        // Eşit bölme (ileride profil kalınlığı düşülebilir)
        double newWidth = isVertical ? node.getWidth() / 2 : node.getWidth();
        double newHeight = isVertical ? node.getHeight() : node.getHeight() / 2;

        // Sol / Üst çocuk
        WindowNode child1 = new WindowNode();
        child1.setNodeType(NodeType.EMPTY);
        child1.setWidth(newWidth);
        child1.setHeight(newHeight);
        child1.setItemOrder(0);
        node.addChild(child1);

        // Sağ / Alt çocuk
        WindowNode child2 = new WindowNode();
        child2.setNodeType(NodeType.EMPTY);
        child2.setWidth(newWidth);
        child2.setHeight(newHeight);
        child2.setItemOrder(1);
        node.addChild(child2);

        return windowNodeRepository.save(node);
    }

    @Override
    @Transactional
    public WindowNode updateNodeType(Long nodeId, String nodeTypeStr) {

        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        try {
            NodeType newType = NodeType.valueOf(nodeTypeStr);

            // Taşıyıcı elemanlar değiştirilemez
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

            // İleride nodeType ↔ profileType validasyonu eklenebilir
            node.setProfile(profile);
            node.setGlass(null);

        } else if ("GLASS".equalsIgnoreCase(materialType)) {

            Glass glass = glassRepository.findById(materialId)
                    .orElseThrow(() -> new RuntimeException("Cam bulunamadı"));

            node.setGlass(glass);
            node.setProfile(null);

        } else {
            throw new RuntimeException("Geçersiz materialType: " + materialType);
        }

        return windowNodeRepository.save(node);
    }
}
