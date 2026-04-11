package com.aimpl.domain.workforce.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.workforce.service.DailyTaskGeneratorService;
import com.aimpl.domain.workforce.vo.DailyReportDraftVO;
import com.aimpl.domain.workforce.vo.DailyTaskPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/workforce")
@RequiredArgsConstructor
public class DailyTaskController {

    private final DailyTaskGeneratorService dailyTaskGeneratorService;

    @GetMapping("/daily-tasks")
    public R<DailyTaskPlanVO> getDailyTasks(
            @RequestParam Long engineerId,
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(dailyTaskGeneratorService.generateDailyTasks(engineerId, projectId, date));
    }

    @GetMapping("/report-draft")
    public R<DailyReportDraftVO> getReportDraft(
            @RequestParam Long engineerId,
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(dailyTaskGeneratorService.generateReportDraft(engineerId, projectId, date));
    }
}
