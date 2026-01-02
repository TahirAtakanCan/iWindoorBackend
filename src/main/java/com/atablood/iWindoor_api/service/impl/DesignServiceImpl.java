package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.NodeType;
import com.atablood.iWindoor_api.entity.WindowNode;
import com.atablood.iWindoor_api.repository.WindowNodeRepository;
import com.atablood.iWindoor_api.service.DesignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignServiceImpl implements DesignService {

    private final WindowNodeRepository windowNodeRepository;

    @Override
    @Transactional
    public WindowNode splitNode(Long nodeId, boolean isVertical) {
        // 1. Düğümü bul
        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        // 2. Bu parça bölünebilir mi? (Sadece KASA veya BOŞLUK bölünebilir diyelim şimdilik)
        // İstersen buraya kural ekleyebilirsin.

        // 3. Düğümün tipini değiştir (Konteyner yapıyoruz)
        node.setNodeType(isVertical ? NodeType.MULLION_VERTICAL : NodeType.MULLION_HORIZONTAL);

        // 4. İki tane yeni çocuk (Boşluk) oluştur
        // Boyut hesaplarını şimdilik "Eşit Bölme" varsayıyoruz.
        // İleride buraya matematik girecek (Profil kalınlığı düşülecek vs.)

        double newWidth = isVertical ? node.getWidth() / 2 : node.getWidth();
        double newHeight = isVertical ? node.getHeight() : node.getHeight() / 2;

        // Çocuk 1 (Sol veya Üst)
        WindowNode child1 = new WindowNode();
        child1.setNodeType(NodeType.EMPTY); // Henüz boş
        child1.setWidth(newWidth);
        child1.setHeight(newHeight);
        child1.setItemOrder(0);
        node.addChild(child1); // Parent'a bağla

        // Çocuk 2 (Sağ veya Alt)
        WindowNode child2 = new WindowNode();
        child2.setNodeType(NodeType.EMPTY);
        child2.setWidth(newWidth);
        child2.setHeight(newHeight);
        child2.setItemOrder(1);
        node.addChild(child2);

        // 5. Kaydet ve DÖNDÜR
        return windowNodeRepository.save(node);
    }

    @Override
    @Transactional
    public WindowNode updateNodeType(Long nodeId, String nodeTypeStr) {
        WindowNode node = windowNodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Parça bulunamadı"));

        // Gelen String'i Enum'a çevir (GLASS, SASH vb.)
        try {
            NodeType newType = NodeType.valueOf(nodeTypeStr);

            // Validasyon: Sadece EMPTY olanlar veya zaten dolu olanlar değiştirilebilir
            // Kasa (FRAME) veya Kayıt (MULLION) değiştirilemez (şimdilik basit tutalım)
            if (node.getNodeType() == NodeType.FRAME ||
                    node.getNodeType() == NodeType.MULLION_VERTICAL ||
                    node.getNodeType() == NodeType.MULLION_HORIZONTAL) {
                throw new RuntimeException("Taşıyıcı parçalar değiştirilemez!");
            }

            node.setNodeType(newType);

            // Eğer "SASH" (Kanat) seçildiyse, içine otomatik CAM da eklemek gerekebilir
            // Ama şimdilik sadece tipini değiştiriyoruz.

            return windowNodeRepository.save(node);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz parça tipi: " + nodeTypeStr);
        }
    }
}
