package com.aimpl.domain.workforce.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.workforce.dto.ProjectEvaluationCreateDTO;
import com.aimpl.domain.workforce.entity.ProjectEvaluation;
import com.aimpl.domain.workforce.service.ProjectEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-evaluations")
@RequiredArgsConstructor
public class ProjectEvaluationController {

    private final ProjectEvaluationService projectEvaluationService;

    @PostMapping
    public R<ProjectEvaluation> create(@Valid @RequestBody ProjectEvaluationCreateDTO dto) {
        return R.ok(projectEvaluationService.create(dto));
    }

    @GetMapping
    public R<List<ProjectEvaluation>> list(@RequestParam Long projectId) {
        return R.ok(projectEvaluationService.listByProjectId(projectId));
    }
}
