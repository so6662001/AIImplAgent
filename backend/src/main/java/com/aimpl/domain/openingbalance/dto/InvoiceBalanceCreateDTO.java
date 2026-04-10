package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "发票类型不能为空")
    @Size(max = 20, message = "发票类型长度不能超过20")
    private String invoiceType;

    @NotNull(message = "往来单位ID不能为空")
    private Long counterpartyId;

    @Size(max = 120, message = "往来单位名称长度不能超过120")
    private String counterpartyName;

    @Size(max = 40, message = "单据编号长度不能超过40")
    private String docNo;

    @NotNull(message = "货品ID不能为空")
    private Long productId;

    @Size(max = 60, message = "货品名称长度不能超过60")
    private String productName;

    @Size(max = 80, message = "规格长度不能超过80")
    private String spec;

    @Size(max = 30, message = "材质长度不能超过30")
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

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
