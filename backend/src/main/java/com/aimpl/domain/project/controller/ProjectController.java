package com.aimpl.domain.project.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public R<Project> create(@Valid @RequestBody ProjectCreateDTO dto) {
        return R.ok(projectService.createProject(dto));
    }

    @GetMapping("/{id}")
    public R<Project> get(@PathVariable Long id) {
        return R.ok(projectService.getById(id));
    }

    @GetMapping
    public R<List<Project>> list() {
        return R.ok(projectService.list());
    }
}
