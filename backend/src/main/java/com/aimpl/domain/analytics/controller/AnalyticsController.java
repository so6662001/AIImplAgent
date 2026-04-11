package com.aimpl.domain.analytics.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.analytics.service.AnalyticsService;
import com.aimpl.domain.analytics.vo.DepartmentAnalyticsVO;
import com.aimpl.domain.analytics.vo.ProjectAnalyticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/project/{projectId}")
    public R<ProjectAnalyticsVO> getProjectAnalytics(@PathVariable Long projectId) {
        return R.ok(analyticsService.getProjectAnalytics(projectId));
    }

    @GetMapping("/department")
    public R<DepartmentAnalyticsVO> getDepartmentAnalytics() {
        return R.ok(analyticsService.getDepartmentAnalytics());
    }
}
