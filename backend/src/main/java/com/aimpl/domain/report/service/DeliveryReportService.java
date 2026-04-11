package com.aimpl.domain.report.service;

import com.aimpl.common.enums.ReportStatus;
import com.aimpl.common.enums.ReportType;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.report.dto.DeliveryReportCreateDTO;
import com.aimpl.domain.report.entity.DeliveryReport;
import com.aimpl.domain.report.mapper.DeliveryReportMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryReportService extends ServiceImpl<DeliveryReportMapper, DeliveryReport> {

    private final ProjectMapper projectMapper;

    @Transactional
    public DeliveryReport create(DeliveryReportCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        if (dto.getTotalIssues() != null && dto.getResolvedIssues() != null
                && dto.getTotalIssues() < dto.getResolvedIssues()) {
            throw new BizException("已解决问题数不能大于总问题数");
        }

        ReportType reportType;
        try {
            reportType = ReportType.valueOf(dto.getReportType());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的报告类型: " + dto.getReportType());
        }

        DeliveryReport entity = new DeliveryReport();
        entity.setProjectId(dto.getProjectId());
        entity.setTitle(dto.getTitle());
        entity.setReportType(reportType);
        entity.setTrainingPassRate(dto.getTrainingPassRate());
        entity.setDataImportCompletionRate(dto.getDataImportCompletionRate());
        entity.setTotalIssues(dto.getTotalIssues());
        entity.setResolvedIssues(dto.getResolvedIssues());
        entity.setCustomerSatisfactionScore(dto.getCustomerSatisfactionScore());
        entity.setScheduleDeviationPercent(dto.getScheduleDeviationPercent());
        entity.setDocumentCompletionRate(dto.getDocumentCompletionRate());
        entity.setAiComment(dto.getAiComment());
        entity.setStatus(ReportStatus.DRAFT);
        entity.setOverallScore(calculateOverallScore(dto));
        save(entity);
        return entity;
    }

    public List<DeliveryReport> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<DeliveryReport>()
                .eq(DeliveryReport::getProjectId, projectId)
                .orderByDesc(DeliveryReport::getCreateTime));
    }

    @Transactional
    public DeliveryReport confirm(Long id, String confirmedBy) {
        DeliveryReport report = getById(id);
        if (report == null) {
            throw new BizException("交付报告不存在: " + id);
        }
        if (report.getStatus() == ReportStatus.CONFIRMED) {
            throw new BizException("报告已确认，不能重复确认");
        }
        report.setStatus(ReportStatus.CONFIRMED);
        report.setConfirmedBy(confirmedBy);
        report.setConfirmedAt(LocalDateTime.now());
        updateById(report);
        return report;
    }

    /**
     * Weighted average:
     * trainingPassRate(20%) + dataImportCompletionRate(20%) + issueResolutionRate(15%)
     * + customerSatisfaction*20(15%) + (100-|scheduleDeviation|)(15%) + documentCompletionRate(15%)
     */
    private BigDecimal calculateOverallScore(DeliveryReportCreateDTO dto) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;

        if (dto.getTrainingPassRate() != null) {
            total = total.add(dto.getTrainingPassRate().multiply(new BigDecimal("0.20")));
            weightSum = weightSum.add(new BigDecimal("0.20"));
        }
        if (dto.getDataImportCompletionRate() != null) {
            total = total.add(dto.getDataImportCompletionRate().multiply(new BigDecimal("0.20")));
            weightSum = weightSum.add(new BigDecimal("0.20"));
        }
        if (dto.getTotalIssues() != null && dto.getTotalIssues() > 0 && dto.getResolvedIssues() != null) {
            BigDecimal issueRate = new BigDecimal(dto.getResolvedIssues())
                    .divide(new BigDecimal(dto.getTotalIssues()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            total = total.add(issueRate.multiply(new BigDecimal("0.15")));
            weightSum = weightSum.add(new BigDecimal("0.15"));
        }
        if (dto.getCustomerSatisfactionScore() != null) {
            BigDecimal csatPercent = dto.getCustomerSatisfactionScore().multiply(new BigDecimal("20"));
            total = total.add(csatPercent.multiply(new BigDecimal("0.15")));
            weightSum = weightSum.add(new BigDecimal("0.15"));
        }
        if (dto.getScheduleDeviationPercent() != null) {
            BigDecimal schedScore = new BigDecimal("100")
                    .subtract(dto.getScheduleDeviationPercent().abs())
                    .max(BigDecimal.ZERO);
            total = total.add(schedScore.multiply(new BigDecimal("0.15")));
            weightSum = weightSum.add(new BigDecimal("0.15"));
        }
        if (dto.getDocumentCompletionRate() != null) {
            total = total.add(dto.getDocumentCompletionRate().multiply(new BigDecimal("0.15")));
            weightSum = weightSum.add(new BigDecimal("0.15"));
        }

        if (weightSum.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(weightSum, 2, RoundingMode.HALF_UP);
    }
}
