package com.aimpl.domain.dataimport.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.dataimport.service.ImportOrchestrationService;
import com.aimpl.domain.dataimport.service.ImportStatusService;
import com.aimpl.domain.dataimport.service.ReconciliationService;
import com.aimpl.domain.dataimport.vo.ImportProgressVO;
import com.aimpl.domain.dataimport.vo.ImportStatusOverviewVO;
import com.aimpl.domain.dataimport.vo.ReconciliationResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data-import")
@RequiredArgsConstructor
public class DataImportController {

    private final ImportOrchestrationService importOrchestrationService;
    private final ReconciliationService reconciliationService;
    private final ImportStatusService importStatusService;

    @PostMapping("/init/{projectId}")
    public R<ImportProgressVO> initProgress(@PathVariable Long projectId) {
        return R.ok(importOrchestrationService.initProgress(projectId));
    }

    @GetMapping("/progress/{projectId}")
    public R<ImportProgressVO> getProgress(@PathVariable Long projectId) {
        return R.ok(importOrchestrationService.getProgress(projectId));
    }

    @PostMapping("/complete-batch/{projectId}/{batchNumber}")
    public R<ImportProgressVO> completeBatch(@PathVariable Long projectId,
                                             @PathVariable int batchNumber) {
        return R.ok(importOrchestrationService.completeBatch(projectId, batchNumber));
    }

    @PostMapping("/advance/{projectId}")
    public R<ImportProgressVO> advanceBatch(@PathVariable Long projectId) {
        return R.ok(importOrchestrationService.advanceBatch(projectId));
    }

    @GetMapping("/reconciliation/{projectId}")
    public R<ReconciliationResultVO> reconcile(@PathVariable Long projectId) {
        return R.ok(reconciliationService.reconcile(projectId));
    }

    @GetMapping("/overview/{projectId}")
    public R<ImportStatusOverviewVO> getOverview(@PathVariable Long projectId) {
        return R.ok(importStatusService.getOverview(projectId));
    }
}
