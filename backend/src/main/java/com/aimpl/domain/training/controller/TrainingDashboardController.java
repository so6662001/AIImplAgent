package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.TrainingDashboardDTO;
import com.aimpl.domain.training.service.TrainingDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training-dashboard")
@RequiredArgsConstructor
public class TrainingDashboardController {

    private final TrainingDashboardService trainingDashboardService;

    @GetMapping("/{projectId}")
    public R<TrainingDashboardDTO> getDashboard(@PathVariable Long projectId) {
        return R.ok(trainingDashboardService.getDashboard(projectId));
    }
}
