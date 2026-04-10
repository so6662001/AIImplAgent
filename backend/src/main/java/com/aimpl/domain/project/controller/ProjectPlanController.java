package com.aimpl.domain.project.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.project.dto.PlanGenerateRequestDTO;
import com.aimpl.domain.project.dto.ProjectPlanCreateDTO;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.service.PlanGeneratorService;
import com.aimpl.domain.project.service.ProjectPlanService;
import com.aimpl.domain.project.vo.GeneratedPlanVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-plans")
@RequiredArgsConstructor
public class ProjectPlanController {

    private final ProjectPlanService projectPlanService;
    private final PlanGeneratorService planGeneratorService;

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

    @PostMapping("/generate")
    public R<GeneratedPlanVO> generate(@Valid @RequestBody PlanGenerateRequestDTO request) {
        GeneratedPlanVO vo = planGeneratorService.generatePlan(request);
        projectPlanService.saveGeneratedPlan(request.getProjectId(), vo, request);
        return R.ok(vo);
    }

    @GetMapping("/{id}/detail")
    public R<GeneratedPlanVO> detail(@PathVariable Long id) {
        return R.ok(projectPlanService.getGeneratedPlanDetail(id));
    }
}
