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

    private String managementGoals;

    private String processGoals;

    private String efficiencyGoals;

    private String riskControlGoals;
}
