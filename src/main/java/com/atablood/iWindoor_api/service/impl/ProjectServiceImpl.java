package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.*;
import com.atablood.iWindoor_api.repository.ProjectRepository;
import com.atablood.iWindoor_api.repository.WindowUnitRepository;
import com.atablood.iWindoor_api.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final WindowUnitRepository windowUnitRepository;

    @Override
    public Project createProject(String customerName, String description) {
        Project project = new Project();
        project.setName(customerName);
        project.setDescription(description);
        // Varsayılan değerler
        project.setTotalPrice(java.math.BigDecimal.ZERO);
        return projectRepository.save(project);
    }

    @Override
    public Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proje bulunamadı: " + id));
    }

    @Override
    @Transactional // Bu işlem bir bütün olarak yapılmalı (Hata olursa geri al)
    public WindowUnit addWindowToProject(Long projectId, String windowName, Double width, Double height) {
        Project project = getProject(projectId);

        WindowUnit unit = new WindowUnit();
        unit.setProject(project);
        unit.setName(windowName);
        unit.setWidth(width);
        unit.setHeight(height);
        unit.setQuantity(1);

        // OTOMATİK KÖK OLUŞTURMA (Magic Part 🪄)
        // Her pencere boş bir "FRAME" (Kasa) düğümü ile başlar.
        WindowNode rootNode = new WindowNode();
        rootNode.setNodeType(NodeType.FRAME); // Kök her zaman kasadır
        rootNode.setWidth(width);
        rootNode.setHeight(height);
        rootNode.setItemOrder(0);

        // İlişkiyi kur
        unit.setRootNode(rootNode);

        return windowUnitRepository.save(unit);
    }
}
