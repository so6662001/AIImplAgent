package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.TrainingDailyLogCreateDTO;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.service.TrainingDailyLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training-logs")
@RequiredArgsConstructor
public class TrainingDailyLogController {

    private final TrainingDailyLogService trainingDailyLogService;

    @PostMapping
    public R<TrainingDailyLog> create(@Valid @RequestBody TrainingDailyLogCreateDTO dto) {
        return R.ok(trainingDailyLogService.createLog(dto));
    }

    @GetMapping
    public R<List<TrainingDailyLog>> list(@RequestParam Long projectId) {
        return R.ok(trainingDailyLogService.listByProject(projectId));
    }

    @PutMapping("/{id}")
    public R<TrainingDailyLog> update(@PathVariable Long id, @RequestBody TrainingDailyLog update) {
        return R.ok(trainingDailyLogService.updateLog(id, update));
    }
}
