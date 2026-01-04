package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.*;
import com.atablood.iWindoor_api.repository.ProjectRepository;
import com.atablood.iWindoor_api.repository.WindowUnitRepository;
import com.atablood.iWindoor_api.repository.WindowNodeRepository;
import com.atablood.iWindoor_api.service.PricingService; // PricingService Eklendi
import com.atablood.iWindoor_api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WindowUnitRepository windowUnitRepository;
    private final WindowNodeRepository windowNodeRepository;
    private final PricingService pricingService; // Hesaplama için gerekli

    @Override
    public Project createProject(String customerName, String description) {
        return null;
    }

    @Override
    public Project createProject(Project project) {
        if (project.getTotalPrice() == null) project.setTotalPrice(java.math.BigDecimal.ZERO);
        if (project.getWindowUnits() == null) project.setWindowUnits(new java.util.ArrayList<>());
        return projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı: " + id));
    }

    @Override
    @Transactional
    public WindowUnit addWindowToProject(Long projectId, String windowName, Double width, Double height) {
        Project project = getProject(projectId);

        WindowUnit unit = new WindowUnit();
        unit.setProject(project);
        unit.setName(windowName);
        unit.setWidth(width);
        unit.setHeight(height);
        unit.setQuantity(1);

        unit = windowUnitRepository.save(unit);

        WindowNode rootNode = new WindowNode();
        rootNode.setNodeType(NodeType.FRAME);
        rootNode.setWidth(width);
        rootNode.setHeight(height);
        rootNode.setItemOrder(0);

        // --- YENİ: Varsayılan Fiyat (Eğer bir default profil varsa burada set edilebilir) ---
        // Şimdilik null bırakıyoruz, kullanıcı malzeme atayınca fiyat gelecek.

        rootNode = windowNodeRepository.save(rootNode);
        unit.setRootNode(rootNode);

        return windowUnitRepository.save(unit);
    }

    @Override
    public Project updateProject(Long id, Project projectDetails) {
        Project project = getProject(id);
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        return projectRepository.save(project);
    }

    @Override
    public void deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
        } else {
            throw new RuntimeException("Silinecek proje bulunamadı: " + id);
        }
    }

    // --- YENİ: FİYATLARI GÜNCEL KURA ÇEK (SYNC) ---
    @Override
    @Transactional
    public void syncProjectPrices(Long projectId) {
        Project project = getProject(projectId);

        // Tüm pencereleri gez
        for (WindowUnit unit : project.getWindowUnits()) {
            if (unit.getRootNode() != null) {
                updateNodePricesRecursive(unit.getRootNode());
            }
        }

        // Fiyatları yeniden hesapla ve kaydet
        pricingService.calculateProjectPrice(projectId);
    }

    private void updateNodePricesRecursive(WindowNode node) {
        // Profili varsa, storedPrice'ı güncel katalog fiyatıyla güncelle
        if (node.getProfile() != null) {
            node.setStoredPrice(node.getProfile().getPricePerMeter());
        }
        // Camı varsa, storedPrice'ı güncel katalog fiyatıyla güncelle
        if (node.getGlass() != null) {
            node.setStoredPrice(node.getGlass().getPricePerSquareMeter());
        }

        windowNodeRepository.save(node); // Güncellenen fiyatı kaydet

        for (WindowNode child : node.getChildren()) {
            updateNodePricesRecursive(child);
        }
    }
}