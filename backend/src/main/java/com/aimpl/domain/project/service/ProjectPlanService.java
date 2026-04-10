package com.aimpl.domain.project.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.dto.ProjectPlanCreateDTO;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.project.mapper.ProjectPlanMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectPlanService extends ServiceImpl<ProjectPlanMapper, ProjectPlan> {

    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectPlan createPlan(ProjectPlanCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        if (count(new LambdaQueryWrapper<ProjectPlan>()
                .eq(ProjectPlan::getProjectId, dto.getProjectId())) > 0) {
            throw new BizException("该项目已存在计划");
        }

        ProjectPlan plan = new ProjectPlan();
        plan.setProjectId(dto.getProjectId());
        plan.setPlanName(dto.getPlanName());
        plan.setTotalDays(dto.getTotalDays());
        plan.setMilestones(dto.getMilestones());
        plan.setWbsItems(dto.getWbsItems());
        plan.setResources(dto.getResources());
        plan.setRisks(dto.getRisks());
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : "DRAFT");
        save(plan);
        return plan;
    }

    public List<ProjectPlan> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<ProjectPlan>()
                .eq(ProjectPlan::getProjectId, projectId));
    }
}
