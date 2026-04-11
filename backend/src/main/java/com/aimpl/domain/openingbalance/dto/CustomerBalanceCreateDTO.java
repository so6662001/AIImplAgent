package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @Size(max = 30, message = "单据类型长度不能超过30")
    private String docType;

    @Size(max = 40, message = "单据编号长度不能超过40")
    private String docNo;

    private LocalDate docDate;

    private BigDecimal receivableAmount;

    private BigDecimal receivedAmount;

    @NotNull(message = "余额不能为空")
    private BigDecimal balance;

    private LocalDate expectedDate;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
