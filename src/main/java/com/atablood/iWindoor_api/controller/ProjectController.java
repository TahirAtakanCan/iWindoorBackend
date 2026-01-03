package com.atablood.iWindoor_api.controller;

import com.atablood.iWindoor_api.entity.Project;
import com.atablood.iWindoor_api.repository.ProjectRepository;
import com.atablood.iWindoor_api.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final PricingService pricingService;

    // 1. TÜM PROJELERİ GETİR (LİSTE)
    // GET /api/v1/projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    // 2. TEK PROJE GETİR (DETAY)
    // GET /api/v1/projects/1
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. YENİ PROJE OLUŞTUR
    // POST /api/v1/projects
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        // Yeni proje oluştururken fiyatı 0, listesi boş başlar
        project.setTotalPrice(java.math.BigDecimal.ZERO);
        project.setWindowUnits(new java.util.ArrayList<>());

        return ResponseEntity.ok(projectRepository.save(project));
    }

    // 4. FİYAT HESAPLA
    // POST /api/v1/projects/1/calculate-price
    @PostMapping("/{id}/calculate-price")
    public ResponseEntity<Void> calculatePrice(@PathVariable Long id) {
        pricingService.calculateProjectPrice(id);
        return ResponseEntity.ok().build();
    }
}