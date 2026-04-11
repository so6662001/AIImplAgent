package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_invoice_balance")
public class InvoiceBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String invoiceType;

    private Long counterpartyId;

    private String counterpartyName;

    private String docNo;

    private Long productId;

    private String productName;

    private String spec;

    private String material;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private LocalDate docDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
