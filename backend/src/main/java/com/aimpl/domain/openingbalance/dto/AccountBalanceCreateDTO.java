package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "银行账户ID不能为空")
    private Long bankAccountId;

    private String currency;

    @NotNull(message = "期初余额不能为空")
    private BigDecimal openingBalance;

    private String remark;
}
