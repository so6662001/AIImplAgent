package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SupplierBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    private String docType;

    private String docNo;

    private LocalDate docDate;

    private BigDecimal payableAmount;

    private BigDecimal paidAmount;

    @NotNull(message = "余额不能为空")
    private BigDecimal balance;

    private LocalDate expectedDate;

    private String remark;
}
