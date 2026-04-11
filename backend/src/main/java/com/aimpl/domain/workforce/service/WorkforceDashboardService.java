package com.aimpl.domain.workforce.service;

import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.vo.WorkforceDashboardVO;
import com.aimpl.domain.workforce.vo.WorkforceDashboardVO.EngineerStatusRowVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkforceDashboardService {

    private final EngineerMapper engineerMapper;
    private final ProjectMapper projectMapper;

    public WorkforceDashboardVO getDashboard() {
        List<Engineer> allEngineers = engineerMapper.selectList(
                new LambdaQueryWrapper<Engineer>().orderByDesc(Engineer::getCompositeScore)
        );

        int total = allEngineers.size();
        int onProject = 0, idle = 0, training = 0, leave = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;
        int scoreCount = 0;

        for (Engineer e : allEngineers) {
            if (e.getCurrentStatus() == EngineerStatus.ON_PROJECT) onProject++;
            else if (e.getCurrentStatus() == EngineerStatus.IDLE) idle++;
            else if (e.getCurrentStatus() == EngineerStatus.TRAINING) training++;
            else if (e.getCurrentStatus() == EngineerStatus.LEAVE) leave++;

            if (e.getCompositeScore() != null) {
                scoreSum = scoreSum.add(e.getCompositeScore());
                scoreCount++;
            }
        }

        WorkforceDashboardVO vo = new WorkforceDashboardVO();
        vo.setTotalEngineers(total);
        vo.setOnProjectCount(onProject);
        vo.setIdleCount(idle);
        vo.setTrainingCount(training);
        vo.setLeaveCount(leave);

        if (total > 0) {
            vo.setUtilizationRate(BigDecimal.valueOf(onProject)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
            vo.setIdleRate(BigDecimal.valueOf(idle)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        } else {
            vo.setUtilizationRate(BigDecimal.ZERO);
            vo.setIdleRate(BigDecimal.ZERO);
        }

        vo.setAvgCompositeScore(scoreCount > 0
                ? scoreSum.divide(BigDecimal.valueOf(scoreCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        List<EngineerStatusRowVO> rows = new ArrayList<>();
        for (Engineer e : allEngineers) {
            EngineerStatusRowVO row = new EngineerStatusRowVO();
            row.setEngineerId(e.getId());
            row.setName(e.getName());
            row.setLevel(e.getLevel() != null ? e.getLevel().getLabel() : null);
            row.setCurrentStatus(e.getCurrentStatus() != null ? e.getCurrentStatus().getLabel() : null);

            if (e.getCurrentProjectId() != null) {
                Project project = projectMapper.selectById(e.getCurrentProjectId());
                if (project != null) {
                    row.setCurrentProjectName(project.getProjectCode());
                    row.setExpectedRelease(project.getEndDate());
                }
            }

            row.setCompositeScore(e.getCompositeScore());
            row.setMonthlyProjectCount(e.getMonthlyProjectCount() != null ? e.getMonthlyProjectCount() : 0);
            row.setMonthlyIdleRate(e.getMonthlyIdleRate());
            rows.add(row);
        }
        vo.setEngineers(rows);

        return vo;
    }
}
