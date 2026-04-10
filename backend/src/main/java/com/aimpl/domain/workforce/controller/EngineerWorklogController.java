package com.aimpl.domain.workforce.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.workforce.dto.EngineerWorklogCreateDTO;
import com.aimpl.domain.workforce.entity.EngineerWorklog;
import com.aimpl.domain.workforce.service.EngineerWorklogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engineer-worklogs")
@RequiredArgsConstructor
public class EngineerWorklogController {

    private final EngineerWorklogService engineerWorklogService;

    @PostMapping
    public R<EngineerWorklog> create(@Valid @RequestBody EngineerWorklogCreateDTO dto) {
        return R.ok(engineerWorklogService.createWorklog(dto));
    }

    @GetMapping
    public R<List<EngineerWorklog>> list(
            @RequestParam(required = false) Long engineerId,
            @RequestParam(required = false) Long projectId) {
        return R.ok(engineerWorklogService.listByQuery(engineerId, projectId));
    }

    @PutMapping("/{id}/submit")
    public R<EngineerWorklog> submit(@PathVariable Long id) {
        return R.ok(engineerWorklogService.submitWorklog(id));
    }
}
