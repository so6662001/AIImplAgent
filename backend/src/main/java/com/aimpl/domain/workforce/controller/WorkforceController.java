package com.aimpl.domain.workforce.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.workforce.service.CompositeScoreService;
import com.aimpl.domain.workforce.service.MonthlyReportService;
import com.aimpl.domain.workforce.service.WorkforceDashboardService;
import com.aimpl.domain.workforce.vo.CompositeScoreVO;
import com.aimpl.domain.workforce.vo.MonthlyWorkforceReportVO;
import com.aimpl.domain.workforce.vo.WorkforceDashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workforce")
@RequiredArgsConstructor
public class WorkforceController {

    private final WorkforceDashboardService workforceDashboardService;
    private final CompositeScoreService compositeScoreService;
    private final MonthlyReportService monthlyReportService;

    @GetMapping("/dashboard")
    public R<WorkforceDashboardVO> getDashboard() {
        return R.ok(workforceDashboardService.getDashboard());
    }

    @GetMapping("/composite-score/{engineerId}")
    public R<CompositeScoreVO> getCompositeScore(@PathVariable Long engineerId) {
        return R.ok(compositeScoreService.calculateCompositeScore(engineerId));
    }

    @GetMapping("/monthly-report")
    public R<MonthlyWorkforceReportVO> getMonthlyReport() {
        return R.ok(monthlyReportService.generateMonthlyReport());
    }
}
