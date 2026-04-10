package com.aimpl.domain.golive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.golive.dto.GoLiveCheckUpdateDTO;
import com.aimpl.domain.golive.entity.GoLiveCheckItem;
import com.aimpl.domain.golive.service.GoLiveCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/go-live-checks")
@RequiredArgsConstructor
public class GoLiveCheckController {

    private final GoLiveCheckService goLiveCheckService;

    @PostMapping("/init/{projectId}")
    public R<List<GoLiveCheckItem>> init(@PathVariable Long projectId) {
        return R.ok(goLiveCheckService.initChecklist(projectId));
    }

    @GetMapping
    public R<List<GoLiveCheckItem>> list(@RequestParam Long projectId) {
        return R.ok(goLiveCheckService.listByProjectId(projectId));
    }

    @PutMapping("/{id}")
    public R<GoLiveCheckItem> update(@PathVariable Long id, @Valid @RequestBody GoLiveCheckUpdateDTO dto) {
        return R.ok(goLiveCheckService.updateCheck(id, dto));
    }

    @GetMapping("/readiness/{projectId}")
    public R<Map<String, Object>> readiness(@PathVariable Long projectId) {
        return R.ok(goLiveCheckService.getReadiness(projectId));
    }
}
