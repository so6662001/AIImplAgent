package com.aimpl.domain.report.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.report.dto.DeliveryReportCreateDTO;
import com.aimpl.domain.report.entity.DeliveryReport;
import com.aimpl.domain.report.service.DeliveryReportGeneratorService;
import com.aimpl.domain.report.service.DeliveryReportService;
import com.aimpl.domain.report.vo.GeneratedDeliveryReportVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-reports")
@RequiredArgsConstructor
public class DeliveryReportController {

    private final DeliveryReportService deliveryReportService;
    private final DeliveryReportGeneratorService deliveryReportGeneratorService;

    @PostMapping
    public R<DeliveryReport> create(@Valid @RequestBody DeliveryReportCreateDTO dto) {
        return R.ok(deliveryReportService.create(dto));
    }

    @GetMapping("/{id}")
    public R<DeliveryReport> get(@PathVariable Long id) {
        return R.ok(deliveryReportService.getById(id));
    }

    @GetMapping
    public R<List<DeliveryReport>> list(@RequestParam Long projectId) {
        return R.ok(deliveryReportService.listByProjectId(projectId));
    }

    @PutMapping("/{id}/confirm")
    public R<DeliveryReport> confirm(@PathVariable Long id,
                                     @RequestParam(defaultValue = "system") String confirmedBy) {
        return R.ok(deliveryReportService.confirm(id, confirmedBy));
    }

    @PostMapping("/auto-generate/{projectId}")
    public R<GeneratedDeliveryReportVO> autoGenerate(@PathVariable Long projectId) {
        return R.ok(deliveryReportGeneratorService.autoGenerate(projectId));
    }
}
