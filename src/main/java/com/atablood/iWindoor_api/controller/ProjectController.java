package com.atablood.iWindoor_api.controller;

import com.atablood.iWindoor_api.entity.Project;
import com.atablood.iWindoor_api.entity.WindowUnit;
import com.atablood.iWindoor_api.service.PricingService;
import com.atablood.iWindoor_api.service.ProjectService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final PricingService pricingService;

    // 1. Yeni Proje Oluştur
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestParam String customerName,
                                                 @RequestParam String description) {
        return ResponseEntity.ok(projectService.createProject(customerName, description));
    }

    // 2. Proje Detayını Getir
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    // 3. Projeye Pencere Ekle
    // Parametreleri temiz almak için küçük bir DTO (Data Transfer Object) sınıfı kullanıyoruz
    @PostMapping("/{projectId}/windows")
    public ResponseEntity<WindowUnit> addWindow(@PathVariable Long projectId,
                                                @RequestBody CreateWindowRequest request) {
        return ResponseEntity.ok(
                projectService.addWindowToProject(
                        projectId,
                        request.getName(),
                        request.getWidth(),
                        request.getHeight()
                )
        );
    }

    // POST /api/v1/projects/{id}/calculate-price
    @PostMapping("/{id}/calculate-price")
    public ResponseEntity<Void> calculatePrice(@PathVariable Long id) {
        pricingService.calculateProjectPrice(id);
        return ResponseEntity.ok().build();
    }

    // İstek gövdesi için geçici sınıf (Inner Class)
    // Lombok çalışmıyorsa getter/setter ekle
    @Data
    public static class CreateWindowRequest {
        private String name;
        private Double width;
        private Double height;
    }
}
