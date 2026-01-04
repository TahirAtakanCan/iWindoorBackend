package com.atablood.iWindoor_api.service.impl;

import com.atablood.iWindoor_api.entity.*;
import com.atablood.iWindoor_api.repository.ProjectRepository;
import com.atablood.iWindoor_api.repository.WindowUnitRepository;
import com.atablood.iWindoor_api.repository.WindowNodeRepository; // Bunu da ekledim garanti olsun
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

    @Override
    public Project createProject(String customerName, String description) {
        return null;
    }

    @Override
    public Project createProject(Project project) {
        // Fiyat ve liste null gelirse patlamasın diye önlem
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

        // Önce üniteyi kaydedelim ki bir ID'si olsun (JPA bazen ID olmadan ilişki kurarken kızabilir)
        unit = windowUnitRepository.save(unit);

        // OTOMATİK KÖK OLUŞTURMA (Magic Part 🪄)
        WindowNode rootNode = new WindowNode();
        rootNode.setNodeType(NodeType.FRAME);
        rootNode.setWidth(width);
        rootNode.setHeight(height);
        rootNode.setItemOrder(0);

        // RootNode'u da ayrıca kaydedelim (Cascade ayarına güvenmek yerine garanti yol)
        rootNode = windowNodeRepository.save(rootNode);

        // İlişkiyi kur ve güncelle
        unit.setRootNode(rootNode);
        return windowUnitRepository.save(unit);
    }

    @Override
    public Project updateProject(Long id, Project projectDetails) {
        // Mevcut projeyi bul (Yoksa hata fırlatır)
        Project project = getProject(id);

        // Sadece isim ve açıklamayı güncelle
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
}