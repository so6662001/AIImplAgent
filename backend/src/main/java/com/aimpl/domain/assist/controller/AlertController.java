package com.aimpl.domain.assist.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.assist.entity.SystemAlert;
import com.aimpl.domain.assist.service.AlertMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertMonitorService alertMonitorService;

    @PostMapping("/health-check/{projectId}")
    public R<List<SystemAlert>> healthCheck(@PathVariable Long projectId) {
        return R.ok(alertMonitorService.runProjectHealthCheck(projectId));
    }

    @GetMapping
    public R<List<SystemAlert>> list(@RequestParam Long projectId) {
        return R.ok(alertMonitorService.listActiveAlerts(projectId));
    }

    @PutMapping("/{id}/acknowledge")
    public R<Void> acknowledge(@PathVariable Long id,
                               @RequestParam(defaultValue = "system") String acknowledgedBy) {
        alertMonitorService.acknowledgeAlert(id, acknowledgedBy);
        return R.ok();
    }
}
