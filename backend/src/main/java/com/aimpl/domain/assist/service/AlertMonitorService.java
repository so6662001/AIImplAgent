package com.aimpl.domain.assist.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.assist.entity.SystemAlert;
import com.aimpl.domain.assist.mapper.SystemAlertMapper;
import com.aimpl.domain.dataimport.entity.ImportProgress;
import com.aimpl.domain.dataimport.mapper.ImportProgressMapper;
import com.aimpl.domain.openingbalance.entity.CustomerBalance;
import com.aimpl.domain.openingbalance.entity.InventoryBalance;
import com.aimpl.domain.openingbalance.entity.SubjectBalance;
import com.aimpl.domain.openingbalance.mapper.CustomerBalanceMapper;
import com.aimpl.domain.openingbalance.mapper.InventoryBalanceMapper;
import com.aimpl.domain.openingbalance.mapper.SubjectBalanceMapper;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertMonitorService extends ServiceImpl<SystemAlertMapper, SystemAlert> {

    private final ProjectMapper projectMapper;
    private final InventoryBalanceMapper inventoryBalanceMapper;
    private final CustomerBalanceMapper customerBalanceMapper;
    private final SubjectBalanceMapper subjectBalanceMapper;
    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final ImportProgressMapper importProgressMapper;

    @Transactional
    public List<SystemAlert> runProjectHealthCheck(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        List<SystemAlert> newAlerts = new ArrayList<>();

        checkDataAnomalies(projectId, newAlerts);
        checkTrainingRisks(projectId, newAlerts);
        checkDeadlineWarnings(projectId, project, newAlerts);

        for (SystemAlert alert : newAlerts) {
            if (!isDuplicate(alert)) {
                save(alert);
            }
        }

        return list(new LambdaQueryWrapper<SystemAlert>()
                .eq(SystemAlert::getProjectId, projectId)
                .eq(SystemAlert::getAcknowledged, false)
                .orderByDesc(SystemAlert::getCreateTime));
    }

    @Transactional
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        SystemAlert alert = getById(alertId);
        if (alert == null) {
            throw new BizException("告警不存在: " + alertId);
        }
        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);
        alert.setAcknowledgedAt(LocalDateTime.now());
        updateById(alert);
    }

    public List<SystemAlert> listActiveAlerts(Long projectId) {
        return list(new LambdaQueryWrapper<SystemAlert>()
                .eq(SystemAlert::getProjectId, projectId)
                .eq(SystemAlert::getAcknowledged, false)
                .orderByDesc(SystemAlert::getCreateTime));
    }

    private void checkDataAnomalies(Long projectId, List<SystemAlert> alerts) {
        List<InventoryBalance> inventories = inventoryBalanceMapper.selectList(
                new LambdaQueryWrapper<InventoryBalance>()
                        .eq(InventoryBalance::getProjectId, projectId));
        for (InventoryBalance inv : inventories) {
            if (inv.getCostAmount() != null && inv.getCostAmount().compareTo(BigDecimal.ZERO) <= 0) {
                alerts.add(buildAlert(projectId, "DATA_ANOMALY", "WARNING",
                        "库存成本金额异常",
                        "库存记录(ID:" + inv.getId() + ")成本金额为" + inv.getCostAmount() + "，存在异常",
                        "请核实该库存记录的成本数据是否正确"));
                break;
            }
        }

        List<CustomerBalance> customerBalances = customerBalanceMapper.selectList(
                new LambdaQueryWrapper<CustomerBalance>()
                        .eq(CustomerBalance::getProjectId, projectId));
        for (CustomerBalance cb : customerBalances) {
            if (cb.getDocDate() != null && ChronoUnit.DAYS.between(cb.getDocDate(), LocalDate.now()) > 365) {
                alerts.add(buildAlert(projectId, "DATA_ANOMALY", "WARNING",
                        "存在超长账龄应收",
                        "客户余额记录(ID:" + cb.getId() + ")单据日期超过365天",
                        "请关注超长账龄应收款项，及时进行催收或坏账处理"));
                break;
            }
        }

        List<SubjectBalance> subjectBalances = subjectBalanceMapper.selectList(
                new LambdaQueryWrapper<SubjectBalance>()
                        .eq(SubjectBalance::getProjectId, projectId));
        if (!subjectBalances.isEmpty()) {
            BigDecimal totalDebit = subjectBalances.stream()
                    .map(sb -> sb.getDebitBalance() != null ? sb.getDebitBalance() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalCredit = subjectBalances.stream()
                    .map(sb -> sb.getCreditBalance() != null ? sb.getCreditBalance() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalDebit.compareTo(totalCredit) != 0) {
                alerts.add(buildAlert(projectId, "DATA_ANOMALY", "CRITICAL",
                        "科目余额试算不平衡",
                        "借方合计:" + totalDebit + "，贷方合计:" + totalCredit + "，差额:" + totalDebit.subtract(totalCredit),
                        "请检查科目期初余额数据，确保借贷平衡"));
            }
        }
    }

    private void checkTrainingRisks(Long projectId, List<SystemAlert> alerts) {
        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));
        for (TraineeProfile tp : trainees) {
            if (Boolean.TRUE.equals(tp.getKaUser())
                    && tp.getProgressPercent() != null && tp.getProgressPercent() < 50) {
                alerts.add(buildAlert(projectId, "TRAINING_RISK", "WARNING",
                        "KA用户" + tp.getEmployeeName() + "培训进度不足50%",
                        "KA用户" + tp.getEmployeeName() + "当前培训进度为" + tp.getProgressPercent() + "%",
                        "请重点关注该KA用户的培训进度，安排补课或一对一辅导"));
            }
        }

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        if (!exams.isEmpty()) {
            long total = exams.size();
            long passed = exams.stream().filter(e -> Boolean.TRUE.equals(e.getPassed())).count();
            double passRate = (double) passed / total * 100;
            if (passRate < 70) {
                alerts.add(buildAlert(projectId, "TRAINING_RISK", "WARNING",
                        "整体考核通过率偏低",
                        "当前考核通过率为" + String.format("%.1f", passRate) + "%，低于70%标准",
                        "建议加强培训辅导，针对薄弱模块安排专项练习"));
            }
        }

        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        List<TrainingDailyLog> recentLogs = trainingDailyLogMapper.selectList(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId)
                        .ge(TrainingDailyLog::getLogDate, threeDaysAgo));
        if (recentLogs.isEmpty()) {
            alerts.add(buildAlert(projectId, "TRAINING_RISK", "INFO",
                    "近3天培训日志未提交",
                    "项目近3天内没有提交培训日志记录",
                    "请及时提交培训日志，确保培训过程可追溯"));
        }
    }

    private void checkDeadlineWarnings(Long projectId, Project project, List<SystemAlert> alerts) {
        if (project.getEndDate() != null) {
            long daysUntilEnd = ChronoUnit.DAYS.between(LocalDate.now(), project.getEndDate());
            if (daysUntilEnd >= 0 && daysUntilEnd < 14) {
                alerts.add(buildAlert(projectId, "DEADLINE_WARNING", "WARNING",
                        "项目即将到期",
                        "项目结束日期为" + project.getEndDate() + "，距今仅剩" + daysUntilEnd + "天",
                        "请加快项目推进，确保在截止日期前完成交付"));
            }
        }

        ImportProgress progress = importProgressMapper.selectOne(
                new LambdaQueryWrapper<ImportProgress>()
                        .eq(ImportProgress::getProjectId, projectId));
        if (progress != null && !"COMPLETED".equals(progress.getOverallStatus())) {
            alerts.add(buildAlert(projectId, "DEADLINE_WARNING", "WARNING",
                    "期初数据导入未完成",
                    "当前数据导入状态为" + progress.getOverallStatus(),
                    "请尽快完成期初数据导入，避免影响后续业务流程"));
        }
    }

    private SystemAlert buildAlert(Long projectId, String alertType, String severity,
                                   String title, String description, String suggestion) {
        SystemAlert alert = new SystemAlert();
        alert.setProjectId(projectId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSuggestion(suggestion);
        alert.setAcknowledged(false);
        return alert;
    }

    private boolean isDuplicate(SystemAlert alert) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Long count = lambdaQuery()
                .eq(SystemAlert::getProjectId, alert.getProjectId())
                .eq(SystemAlert::getAlertType, alert.getAlertType())
                .eq(SystemAlert::getTitle, alert.getTitle())
                .ge(SystemAlert::getCreateTime, oneDayAgo)
                .count();
        return count != null && count > 0;
    }
}
