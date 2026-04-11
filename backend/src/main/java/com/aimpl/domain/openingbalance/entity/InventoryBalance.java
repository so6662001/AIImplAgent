package com.aimpl.domain.openingbalance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_inventory_balance")
public class InventoryBalance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long productId;

    private Long warehouseId;

    private Long locationId;

    private String batchNo;

    private LocalDate inboundDate;

    private BigDecimal quantity;

    private BigDecimal weight;

    private BigDecimal packQuantity;

    private BigDecimal costUnitPrice;

    private BigDecimal wholeUnits;

    private BigDecimal oddUnits;

    private BigDecimal costAmount;

    private BigDecimal unitWeight;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
