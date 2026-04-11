package com.aimpl.domain.research.dto;

import com.aimpl.common.enums.BusinessModel;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.TradeMode;
import com.aimpl.common.enums.TradeScope;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerProfileCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "公司名称不能为空")
    @Size(max = 100, message = "公司名称长度不能超过100")
    private String companyName;

    private IndustryType industryType;

    private BusinessModel businessModel;

    private TradeMode tradeMode;

    private TradeScope tradeScope;

    @Size(max = 1000, message = "主营业务长度不能超过1000")
    private String mainBusiness;

    // basic_info extended
    @Size(max = 30, message = "法定代表人长度不能超过30")
    private String legalPerson;

    @Size(max = 30, message = "注册资本长度不能超过30")
    private String registeredCapital;

    private LocalDate establishmentDate;

    @Size(max = 200, message = "办公地址长度不能超过200")
    private String address;

    // production
    @Min(value = 0, message = "产线数量不能为负数")
    private Integer totalProductionLines;

    @Size(max = 20, message = "排班模式长度不能超过20")
    private String productionShifts;

    @Size(max = 20, message = "MES现状长度不能超过20")
    private String mesCurrentStatus;

    @Size(max = 200, message = "质量标准长度不能超过200")
    private String qualityStandards;

    // warehouse & inventory
    @Min(value = 0, message = "仓库数量不能为负数")
    private Integer totalWarehouseCount;

    @DecimalMin(value = "0", message = "仓库面积不能为负数")
    private BigDecimal totalWarehouseAreaSqm;

    @Min(value = 0, message = "行车数量不能为负数")
    private Integer totalCraneCount;

    @DecimalMin(value = "0", message = "库存周转率不能为负数")
    private BigDecimal inventoryTurnoverRate;

    @Size(max = 30, message = "库存管理方式长度不能超过30")
    private String inventoryManagementMethod;

    // sales
    @DecimalMin(value = "0", message = "月度销量不能为负数")
    private BigDecimal monthlyVolume;

    @DecimalMin(value = "0", message = "月度金额不能为负数")
    private BigDecimal monthlyAmount;

    @Size(max = 30, message = "定价模式长度不能超过30")
    private String pricingModel;

    @Size(max = 100, message = "结算方式长度不能超过100")
    private String settlementMethods;

    @Size(max = 200, message = "信用政策长度不能超过200")
    private String creditPolicy;

    @Size(max = 30, message = "销售模式长度不能超过30")
    private String salesMode;

    // customer_base
    @Min(value = 0, message = "客户总数不能为负数")
    private Integer totalCustomerCount;

    @Size(max = 200, message = "客户类型分布长度不能超过200")
    private String customerTypes;

    @Size(max = 500, message = "主要大客户长度不能超过500")
    private String topCustomers;

    // organization
    @Min(value = 0, message = "员工数量不能为负数")
    @Max(value = 100000, message = "员工数量不能超过100000")
    private Integer totalStaff;

    @Size(max = 500, message = "部门设置长度不能超过500")
    private String departments;

    @Size(max = 500, message = "关键岗位长度不能超过500")
    private String keyPositions;

    @Size(max = 200, message = "决策链长度不能超过200")
    private String decisionChain;

    // existing_systems
    @Size(max = 1000, message = "现有系统长度不能超过1000")
    private String existingSystems;

    // project_scope
    @Size(max = 500, message = "目标模块长度不能超过500")
    private String targetModules;

    @Size(max = 500, message = "模块优先级说明长度不能超过500")
    private String modulePriorities;

    // goals
    @Size(max = 1000, message = "管理目标长度不能超过1000")
    private String managementGoals;

    @Size(max = 1000, message = "流程目标长度不能超过1000")
    private String processGoals;

    @Size(max = 1000, message = "效率目标长度不能超过1000")
    private String efficiencyGoals;

    @Size(max = 1000, message = "风控目标长度不能超过1000")
    private String riskControlGoals;
}
