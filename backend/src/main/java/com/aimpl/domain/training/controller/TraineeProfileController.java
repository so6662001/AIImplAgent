package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.TraineeProfileCreateDTO;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.service.TraineeProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
public class TraineeProfileController {

    private final TraineeProfileService traineeProfileService;

    @PostMapping
    public R<TraineeProfile> create(@Valid @RequestBody TraineeProfileCreateDTO dto) {
        return R.ok(traineeProfileService.createTrainee(dto));
    }

    @GetMapping("/{id}")
    public R<TraineeProfile> get(@PathVariable Long id) {
        return R.ok(traineeProfileService.getById(id));
    }

    @GetMapping
    public R<List<TraineeProfile>> list(@RequestParam Long projectId) {
        return R.ok(traineeProfileService.listByProject(projectId));
    }
}
