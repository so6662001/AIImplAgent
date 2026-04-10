package com.aimpl.domain.research.entity;

import com.aimpl.common.enums.BusinessModel;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.TradeMode;
import com.aimpl.common.enums.TradeScope;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
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

    private Integer totalProductionLines;

    private Integer totalWarehouseCount;

    private BigDecimal totalWarehouseAreaSqm;

    private Integer totalCraneCount;

    private BigDecimal monthlyVolume;

    private BigDecimal monthlyAmount;

    private Integer totalStaff;

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
