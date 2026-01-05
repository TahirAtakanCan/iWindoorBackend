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
import com.atablood.iWindoor_api.dto.ProjectSpecsDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    @Override
    public ProjectSpecsDTO getProjectSpecs(Long projectId) {
        Project project = getProject(projectId);
        ProjectSpecsDTO specs = new ProjectSpecsDTO();

        specs.setProjectName(project.getName());
        specs.setTotalWindowCount(project.getWindowUnits().size());

        double totalArea = 0;
        double totalProfileLen = 0;
        Set<String> profileNames = new HashSet<>();
        Set<String> glassNames = new HashSet<>();

        for (WindowUnit unit : project.getWindowUnits()) {
            // Pencere Alanı (m2)
            totalArea += (unit.getWidth() * unit.getHeight()) / 1_000_000.0;

            if (unit.getRootNode() != null) {
                collectSpecsRecursive(unit.getRootNode(), profileNames, glassNames);
                // Basit profil metraj hesabı (daha hassas hesap DesignService'de yapılabilir)
                // Burada sadece istatistik amaçlı yaklaşık değer veriyoruz.
                // Detaylısı MEO4'te (Maliyet Tablosu) var zaten.
            }
        }

        specs.setTotalAreaM2(Math.round(totalArea * 100.0) / 100.0); // 2 hane yuvarla
        specs.setUsedProfileTypes(new ArrayList<>(profileNames));
        specs.setUsedGlassTypes(new ArrayList<>(glassNames));

        return specs;
    }

    private void collectSpecsRecursive(WindowNode node, Set<String> profiles, Set<String> glasses) {
        if (node.getProfile() != null) {
            profiles.add(node.getProfile().getName());
        }
        if (node.getGlass() != null) {
            glasses.add(node.getGlass().getName());
        }

        if (node.getChildren() != null) {
            for (WindowNode child : node.getChildren()) {
                collectSpecsRecursive(child, profiles, glasses);
            }
        }
    }
}