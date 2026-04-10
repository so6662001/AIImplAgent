package com.aimpl.domain.accountset.dto;

import com.aimpl.common.enums.AccountingSystem;
import com.aimpl.common.enums.PricingMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountSetCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "帐套名称不能为空")
    @Size(max = 60, message = "帐套名称长度不能超过60")
    private String setName;

    private AccountingSystem accountingSystem;

    private PricingMethod pricingMethod;

    @Min(value = 0, message = "数量小数位不能为负数")
    @Max(value = 4, message = "数量小数位不能超过4")
    private Integer qtyDecimals;

    @Min(value = 0, message = "重量小数位不能为负数")
    @Max(value = 4, message = "重量小数位不能超过4")
    private Integer wgtDecimals;

    @Min(value = 0, message = "单价小数位不能为负数")
    @Max(value = 4, message = "单价小数位不能超过4")
    private Integer prcDecimals;

    @Min(value = 0, message = "金额小数位不能为负数")
    @Max(value = 2, message = "金额小数位不能超过2")
    private Integer amtDecimals;

    private Boolean useWeight;

    @Size(max = 10, message = "币种长度不能超过10")
    private String currency;

    @Min(value = 1, message = "财年起始月份不能小于1")
    @Max(value = 12, message = "财年起始月份不能大于12")
    private Integer fiscalYearStart;
}
