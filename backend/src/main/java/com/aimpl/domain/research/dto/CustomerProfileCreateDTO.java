package com.aimpl.domain.research.dto;

import com.aimpl.common.enums.BusinessModel;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.TradeMode;
import com.aimpl.common.enums.TradeScope;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

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

    @Min(value = 0, message = "产线数量不能为负数")
    private Integer totalProductionLines;

    private Integer totalWarehouseCount;

    @DecimalMin(value = "0", message = "仓库面积不能为负数")
    private BigDecimal totalWarehouseAreaSqm;

    @Min(value = 0, message = "行车数量不能为负数")
    private Integer totalCraneCount;

    @DecimalMin(value = "0", message = "月度销量不能为负数")
    private BigDecimal monthlyVolume;

    @DecimalMin(value = "0", message = "月度金额不能为负数")
    private BigDecimal monthlyAmount;

    @Min(value = 0, message = "员工数量不能为负数")
    @Max(value = 100000, message = "员工数量不能超过100000")
    private Integer totalStaff;

    @Size(max = 1000, message = "管理目标长度不能超过1000")
    private String managementGoals;

    @Size(max = 1000, message = "流程目标长度不能超过1000")
    private String processGoals;

    @Size(max = 1000, message = "效率目标长度不能超过1000")
    private String efficiencyGoals;

    @Size(max = 1000, message = "风控目标长度不能超过1000")
    private String riskControlGoals;
}
