package com.aimpl.domain.accountset.service;

import com.aimpl.common.enums.AccountSetStatus;
import com.aimpl.common.enums.AccountingSystem;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.PricingMethod;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.accountset.entity.AccountSet;
import com.aimpl.domain.accountset.mapper.AccountSetMapper;
import com.aimpl.domain.accountset.vo.AccountSetRecommendVO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.mapper.CustomerProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountSetRecommendService {

    private final ProjectMapper projectMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final AccountSetMapper accountSetMapper;

    public AccountSetRecommendVO recommend(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        CustomerProfile profile = customerProfileMapper.selectOne(
                new LambdaQueryWrapper<CustomerProfile>()
                        .eq(CustomerProfile::getProjectId, projectId)
                        .last("LIMIT 1"));

        IndustryType industryType = project.getIndustryType();
        String modules = project.getModules();

        String setName = buildSetName(project, profile);
        AccountingSystem accountingSystem = recommendAccountingSystem(profile);
        PricingMethod pricingMethod = recommendPricingMethod(industryType);
        boolean useWeight = recommendUseWeight(modules);

        int qtyDecimals = 0;
        int wgtDecimals = 3;
        int prcDecimals = 2;
        int amtDecimals = 2;
        int fiscalYearStart = 1;
        String currency = "CNY";

        List<String> recommendations = buildRecommendations(
                industryType, pricingMethod, qtyDecimals, wgtDecimals, profile, modules);

        String confidence = determineConfidence(profile);

        AccountSetRecommendVO vo = new AccountSetRecommendVO();
        vo.setProjectId(projectId);
        vo.setRecommendedSetName(setName);
        vo.setRecommendedAccountingSystem(accountingSystem.getLabel());
        vo.setRecommendedPricingMethod(pricingMethod.getLabel());
        vo.setRecommendedUseWeight(useWeight);
        vo.setQtyDecimals(qtyDecimals);
        vo.setWgtDecimals(wgtDecimals);
        vo.setPrcDecimals(prcDecimals);
        vo.setAmtDecimals(amtDecimals);
        vo.setFiscalYearStart(fiscalYearStart);
        vo.setCurrency(currency);
        vo.setRecommendations(recommendations);
        vo.setConfidence(confidence);
        return vo;
    }

    @Transactional
    public AccountSet createFromRecommendation(Long projectId) {
        AccountSetRecommendVO vo = recommend(projectId);

        boolean exists = accountSetMapper.selectCount(
                new LambdaQueryWrapper<AccountSet>()
                        .eq(AccountSet::getProjectId, projectId)) > 0;
        if (exists) {
            throw new BizException("该项目已存在帐套，每个项目仅允许一个帐套");
        }

        AccountSet entity = new AccountSet();
        entity.setProjectId(projectId);
        entity.setSetName(vo.getRecommendedSetName());
        entity.setAccountingSystem(resolveAccountingSystem(vo.getRecommendedAccountingSystem()));
        entity.setPricingMethod(resolvePricingMethod(vo.getRecommendedPricingMethod()));
        entity.setQtyDecimals(vo.getQtyDecimals());
        entity.setWgtDecimals(vo.getWgtDecimals());
        entity.setPrcDecimals(vo.getPrcDecimals());
        entity.setAmtDecimals(vo.getAmtDecimals());
        entity.setUseWeight(vo.getRecommendedUseWeight());
        entity.setCurrency(vo.getCurrency());
        entity.setFiscalYearStart(vo.getFiscalYearStart());
        entity.setStatus(AccountSetStatus.DRAFT);
        accountSetMapper.insert(entity);
        return entity;
    }

    private String buildSetName(Project project, CustomerProfile profile) {
        if (profile != null && profile.getCompanyName() != null && !profile.getCompanyName().isBlank()) {
            return profile.getCompanyName() + "帐套";
        }
        return project.getProjectCode() + "帐套";
    }

    private AccountingSystem recommendAccountingSystem(CustomerProfile profile) {
        if (profile == null || profile.getTotalStaff() == null) {
            return AccountingSystem.ENTERPRISE;
        }
        if (profile.getTotalStaff() <= 50) {
            return AccountingSystem.SMALL_ENTERPRISE;
        }
        return AccountingSystem.ENTERPRISE;
    }

    private PricingMethod recommendPricingMethod(IndustryType industryType) {
        if (industryType == null) {
            return PricingMethod.MOVING_WEIGHTED_AVG;
        }
        return switch (industryType) {
            case STEEL_TRADER -> PricingMethod.MOVING_WEIGHTED_AVG;
            case STEEL_MILL -> PricingMethod.SPECIFIC_ID;
            case PROCESSING_CENTER -> PricingMethod.FIFO;
            case INTEGRATED_SERVICE -> PricingMethod.MOVING_WEIGHTED_AVG;
        };
    }

    private boolean recommendUseWeight(String modules) {
        if (modules != null && !modules.contains("仓储")) {
            return false;
        }
        return true;
    }

    private List<String> buildRecommendations(IndustryType industryType,
                                              PricingMethod pricingMethod,
                                              int qtyDecimals, int wgtDecimals,
                                              CustomerProfile profile,
                                              String modules) {
        List<String> list = new ArrayList<>();
        String industryLabel = industryType != null ? industryType.getLabel() : "综合";
        list.add("建议使用" + pricingMethod.getLabel() + "计价方式，适合" + industryLabel + "企业");
        list.add("建议启用重量管理，钢铁行业以重量计价为主");
        list.add("建议数量精度" + qtyDecimals + "位（整件计数），重量精度" + wgtDecimals + "位（精确到千克）");

        if (modules != null && modules.contains("MES")) {
            list.add("含MES模块，建议启用批次管理配合生产追溯");
        }

        if (profile != null && profile.getTotalWarehouseCount() != null && profile.getTotalWarehouseCount() > 3) {
            list.add("多仓库场景，建议启用库位管理");
        }

        return list;
    }

    private String determineConfidence(CustomerProfile profile) {
        if (profile == null) {
            return "MEDIUM";
        }
        if (profile.getTotalStaff() != null && profile.getCompanyName() != null) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    private AccountingSystem resolveAccountingSystem(String label) {
        for (AccountingSystem s : AccountingSystem.values()) {
            if (s.getLabel().equals(label)) {
                return s;
            }
        }
        return AccountingSystem.ENTERPRISE;
    }

    private PricingMethod resolvePricingMethod(String label) {
        for (PricingMethod m : PricingMethod.values()) {
            if (m.getLabel().equals(label)) {
                return m;
            }
        }
        return PricingMethod.MOVING_WEIGHTED_AVG;
    }
}
