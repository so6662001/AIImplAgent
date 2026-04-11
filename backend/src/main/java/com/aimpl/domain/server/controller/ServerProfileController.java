package com.aimpl.domain.server.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.server.dto.ServerProfileCreateDTO;
import com.aimpl.domain.server.entity.ServerProfile;
import com.aimpl.domain.server.service.ServerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/server-profiles")
@RequiredArgsConstructor
public class ServerProfileController {

    private final ServerProfileService serverProfileService;

    @PostMapping
    public R<ServerProfile> create(@Valid @RequestBody ServerProfileCreateDTO dto) {
        return R.ok(serverProfileService.create(dto));
    }

    @GetMapping("/{id}")
    public R<ServerProfile> get(@PathVariable Long id) {
        return R.ok(serverProfileService.getById(id));
    }

    @GetMapping
    public R<List<ServerProfile>> list(@RequestParam Long projectId) {
        return R.ok(serverProfileService.listByProjectId(projectId));
    }

    @PostMapping("/{id}/health-check")
    public R<ServerProfile> healthCheck(@PathVariable Long id) {
        return R.ok(serverProfileService.healthCheck(id));
    }
}
