package com.aimpl.domain.research.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.research.dto.CustomerProfileCreateDTO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerProfileService extends ServiceImpl<CustomerProfileMapper, CustomerProfile> {

    private final ProjectMapper projectMapper;

    @Transactional
    public CustomerProfile create(CustomerProfileCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        boolean nameExists = count(new LambdaQueryWrapper<CustomerProfile>()
                .eq(CustomerProfile::getProjectId, dto.getProjectId())
                .eq(CustomerProfile::getCompanyName, dto.getCompanyName())) > 0;
        if (nameExists) {
            throw new BizException("该项目下公司名称已存在: " + dto.getCompanyName());
        }

        CustomerProfile entity = new CustomerProfile();
        entity.setProjectId(dto.getProjectId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setIndustryType(dto.getIndustryType());
        entity.setBusinessModel(dto.getBusinessModel());
        entity.setTradeMode(dto.getTradeMode());
        entity.setTradeScope(dto.getTradeScope());
        entity.setMainBusiness(dto.getMainBusiness());
        entity.setTotalProductionLines(dto.getTotalProductionLines());
        entity.setTotalWarehouseCount(dto.getTotalWarehouseCount());
        entity.setTotalWarehouseAreaSqm(dto.getTotalWarehouseAreaSqm());
        entity.setTotalCraneCount(dto.getTotalCraneCount());
        entity.setMonthlyVolume(dto.getMonthlyVolume());
        entity.setMonthlyAmount(dto.getMonthlyAmount());
        entity.setTotalStaff(dto.getTotalStaff());
        entity.setManagementGoals(dto.getManagementGoals());
        entity.setProcessGoals(dto.getProcessGoals());
        entity.setEfficiencyGoals(dto.getEfficiencyGoals());
        entity.setRiskControlGoals(dto.getRiskControlGoals());
        save(entity);
        return entity;
    }

    public List<CustomerProfile> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<CustomerProfile>()
                .eq(CustomerProfile::getProjectId, projectId)
                .orderByDesc(CustomerProfile::getCreateTime));
    }
}
