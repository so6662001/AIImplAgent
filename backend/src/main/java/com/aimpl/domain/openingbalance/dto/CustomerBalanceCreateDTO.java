package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    private String docType;

    private String docNo;

    private LocalDate docDate;

    private BigDecimal receivableAmount;

    private BigDecimal receivedAmount;

    @NotNull(message = "余额不能为空")
    private BigDecimal balance;

    private LocalDate expectedDate;

    private String remark;
}
