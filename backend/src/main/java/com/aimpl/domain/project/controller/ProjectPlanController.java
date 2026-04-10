package com.aimpl.domain.project.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.project.dto.ProjectPlanCreateDTO;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.service.ProjectPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-plans")
@RequiredArgsConstructor
public class ProjectPlanController {

    private final ProjectPlanService projectPlanService;

    @PostMapping
    public R<ProjectPlan> create(@Valid @RequestBody ProjectPlanCreateDTO dto) {
        return R.ok(projectPlanService.createPlan(dto));
    }

    @GetMapping("/{id}")
    public R<ProjectPlan> get(@PathVariable Long id) {
        return R.ok(projectPlanService.getById(id));
    }

    @GetMapping
    public R<List<ProjectPlan>> list(@RequestParam Long projectId) {
        return R.ok(projectPlanService.listByProject(projectId));
    }
}
