package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "银行账户ID不能为空")
    private Long bankAccountId;

    @Size(max = 10, message = "币种长度不能超过10")
    private String currency;

    @NotNull(message = "期初余额不能为空")
    private BigDecimal openingBalance;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
