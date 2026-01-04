package com.atablood.iWindoor_api.service;

import com.atablood.iWindoor_api.entity.Project;
import com.atablood.iWindoor_api.entity.WindowUnit;
import java.util.List;

public interface ProjectService {
    Project createProject(String customerName, String description);

    Project createProject(Project project); // Parametre tipini entity yaptım, controller ile uyumlu olsun
    List<Project> getAllProjects(); // Listeleme eklendi
    Project getProject(Long id);
    // --- YENİ EKLENENLER ---
    Project updateProject(Long id, Project projectDetails);
    void deleteProject(Long id);
    WindowUnit addWindowToProject(Long projectId, String windowName, Double width, Double height);
}