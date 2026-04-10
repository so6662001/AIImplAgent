package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "发票类型不能为空")
    private String invoiceType;

    @NotNull(message = "往来单位ID不能为空")
    private Long counterpartyId;

    private String counterpartyName;

    private String docNo;

    @NotNull(message = "货品ID不能为空")
    private Long productId;

    private String productName;

    private String spec;

    private String material;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", message = "数量不能为负数")
    private BigDecimal quantity;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0", message = "单价不能为负数")
    private BigDecimal unitPrice;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0", message = "金额不能为负数")
    private BigDecimal amount;

    @NotNull(message = "税率不能为空")
    @DecimalMin(value = "0", message = "税率不能为负数")
    @DecimalMax(value = "1", message = "税率不能大于1")
    private BigDecimal taxRate;

    private LocalDate docDate;

    private String remark;
}
