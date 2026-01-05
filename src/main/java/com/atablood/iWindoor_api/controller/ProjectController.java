package com.atablood.iWindoor_api.controller;

import com.atablood.iWindoor_api.dto.ProjectCostSummaryDTO;
import com.atablood.iWindoor_api.entity.Project;
import com.atablood.iWindoor_api.entity.WindowUnit;
import com.atablood.iWindoor_api.service.PricingService;
import com.atablood.iWindoor_api.service.ProjectService; // Service import edildi
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.atablood.iWindoor_api.dto.ProjectSpecsDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    // ARTIK REPOSITORY DEĞİL, SERVICE KULLANIYORUZ
    private final ProjectService projectService;
    private final PricingService pricingService;

    // 1. TÜM PROJELERİ GETİR
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // 2. TEK PROJE GETİR
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    // 3. YENİ PROJE OLUŞTUR
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.createProject(project));
    }

    // 4. FİYAT HESAPLA
    @PostMapping("/{id}/calculate-price")
    public ResponseEntity<Void> calculatePrice(@PathVariable Long id) {
        pricingService.calculateProjectPrice(id);
        return ResponseEntity.ok().build();
    }

    // 5. PROJEYE PENCERE EKLE (VE ROOT NODE OLUŞTUR) 🪟
    // Burası artık Service katmanındaki o "Magic Part"ı çağırıyor!
    @PostMapping("/{id}/windows")
    public ResponseEntity<WindowUnit> addWindow(@PathVariable Long id, @RequestBody Map<String, Object> payload) {

        String name = (String) payload.get("name");
        // JSON'dan gelen sayılar bazen Integer bazen Double olabilir, garantiye alalım:
        Double width = Double.valueOf(payload.get("width").toString());
        Double height = Double.valueOf(payload.get("height").toString());

        // Servise devret, o halletsin
        WindowUnit newUnit = projectService.addWindowToProject(id, name, width, height);

        return ResponseEntity.ok(newUnit);
    }

    // 6. PROJE BİLGİLERİNİ GÜNCELLE
    // PUT /api/v1/projects/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        try {
            // Repository yerine Service kullanıyoruz
            return ResponseEntity.ok(projectService.updateProject(id, projectDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 7. PROJEYİ SİL
    // DELETE /api/v1/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            // Repository yerine Service kullanıyoruz
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 8. MALİYET ÖZETİ GETİR
    // GET /api/v1/projects/{id}/cost-summary
    @GetMapping("/{id}/cost-summary")
    public ResponseEntity<ProjectCostSummaryDTO> getCostSummary(@PathVariable Long id) {
        return ResponseEntity.ok(pricingService.getProjectCostSummary(id));
    }

    // 9. PROJE FİYATLARINI GÜNCEL KURA ÇEK
    // POST /api/v1/projects/{id}/sync-prices
    @PostMapping("/{id}/sync-prices")
    public ResponseEntity<Void> syncPrices(@PathVariable Long id) {
        projectService.syncProjectPrices(id);
        return ResponseEntity.ok().build();
    }

    // 10. PROJE TEKNİK ÖZETİ (MEO5)
    // GET /api/v1/projects/{id}/specs
    @GetMapping("/{id}/specs")
    public ResponseEntity<ProjectSpecsDTO> getProjectSpecs(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectSpecs(id));
    }
}