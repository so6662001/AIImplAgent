package com.aimpl.domain.research.entity;

import com.aimpl.common.enums.BusinessModel;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.TradeMode;
import com.aimpl.common.enums.TradeScope;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_customer_profile")
public class CustomerProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String companyName;

    private IndustryType industryType;

    private BusinessModel businessModel;

    private TradeMode tradeMode;

    private TradeScope tradeScope;

    private String mainBusiness;

    // basic_info extended
    private String legalPerson;

    private String registeredCapital;

    private LocalDate establishmentDate;

    private String address;

    // production
    private Integer totalProductionLines;

    private String productionShifts;

    private String mesCurrentStatus;

    private String qualityStandards;

    // warehouse & inventory
    private Integer totalWarehouseCount;

    private BigDecimal totalWarehouseAreaSqm;

    private Integer totalCraneCount;

    private BigDecimal inventoryTurnoverRate;

    private String inventoryManagementMethod;

    // sales
    private BigDecimal monthlyVolume;

    private BigDecimal monthlyAmount;

    private String pricingModel;

    private String settlementMethods;

    private String creditPolicy;

    private String salesMode;

    // customer_base
    private Integer totalCustomerCount;

    private String customerTypes;

    private String topCustomers;

    // organization
    private Integer totalStaff;

    private String departments;

    private String keyPositions;

    private String decisionChain;

    // existing_systems
    private String existingSystems;

    // project_scope
    private String targetModules;

    private String modulePriorities;

    // goals
    private String managementGoals;

    private String processGoals;

    private String efficiencyGoals;

    private String riskControlGoals;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
