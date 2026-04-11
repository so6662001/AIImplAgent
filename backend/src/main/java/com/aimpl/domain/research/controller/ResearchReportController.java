package com.aimpl.domain.research.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.research.dto.ResearchReportVO;
import com.aimpl.domain.research.entity.ResearchReport;
import com.aimpl.domain.research.service.ReportGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/research/reports")
@RequiredArgsConstructor
public class ResearchReportController {

    private final ReportGeneratorService reportGeneratorService;

    @PostMapping("/generate/{profileId}")
    public R<ResearchReportVO> generate(@PathVariable Long profileId) {
        return R.ok(reportGeneratorService.generateReport(profileId));
    }

    @GetMapping
    public R<List<ResearchReport>> list(@RequestParam Long projectId) {
        return R.ok(reportGeneratorService.listByProjectId(projectId));
    }
}
