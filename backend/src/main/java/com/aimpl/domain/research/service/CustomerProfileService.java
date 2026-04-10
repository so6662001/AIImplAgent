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
        // basic_info extended
        entity.setLegalPerson(dto.getLegalPerson());
        entity.setRegisteredCapital(dto.getRegisteredCapital());
        entity.setEstablishmentDate(dto.getEstablishmentDate());
        entity.setAddress(dto.getAddress());
        // production
        entity.setTotalProductionLines(dto.getTotalProductionLines());
        entity.setProductionShifts(dto.getProductionShifts());
        entity.setMesCurrentStatus(dto.getMesCurrentStatus());
        entity.setQualityStandards(dto.getQualityStandards());
        // warehouse & inventory
        entity.setTotalWarehouseCount(dto.getTotalWarehouseCount());
        entity.setTotalWarehouseAreaSqm(dto.getTotalWarehouseAreaSqm());
        entity.setTotalCraneCount(dto.getTotalCraneCount());
        entity.setInventoryTurnoverRate(dto.getInventoryTurnoverRate());
        entity.setInventoryManagementMethod(dto.getInventoryManagementMethod());
        // sales
        entity.setMonthlyVolume(dto.getMonthlyVolume());
        entity.setMonthlyAmount(dto.getMonthlyAmount());
        entity.setPricingModel(dto.getPricingModel());
        entity.setSettlementMethods(dto.getSettlementMethods());
        entity.setCreditPolicy(dto.getCreditPolicy());
        entity.setSalesMode(dto.getSalesMode());
        // customer_base
        entity.setTotalCustomerCount(dto.getTotalCustomerCount());
        entity.setCustomerTypes(dto.getCustomerTypes());
        entity.setTopCustomers(dto.getTopCustomers());
        // organization
        entity.setTotalStaff(dto.getTotalStaff());
        entity.setDepartments(dto.getDepartments());
        entity.setKeyPositions(dto.getKeyPositions());
        entity.setDecisionChain(dto.getDecisionChain());
        // existing_systems
        entity.setExistingSystems(dto.getExistingSystems());
        // project_scope
        entity.setTargetModules(dto.getTargetModules());
        entity.setModulePriorities(dto.getModulePriorities());
        // goals
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
