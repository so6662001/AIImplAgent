package com.aimpl.domain.project.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {

    public Project createProject(ProjectCreateDTO dto) {
        boolean exists = count(new LambdaQueryWrapper<Project>()
                .eq(Project::getProjectCode, dto.getProjectCode())) > 0;
        if (exists) {
            throw new BizException("项目编号已存在: " + dto.getProjectCode());
        }

        Project project = new Project();
        project.setProjectCode(dto.getProjectCode());
        project.setCustomerName(dto.getCustomerName());
        project.setIndustryType(dto.getIndustryType());
        project.setScale(dto.getScale());
        project.setPmId(dto.getPmId());
        project.setModules(dto.getModules() != null ? String.join(",", dto.getModules()) : null);
        project.setRegion(dto.getRegion());
        project.setStartDate(dto.getStartDate());
        project.setSpecialRequirements(dto.getSpecialRequirements());
        project.setRemark(dto.getRemark());
        project.setStatus(com.aimpl.common.enums.ProjectStatus.PENDING);
        save(project);
        return project;
    }
}
