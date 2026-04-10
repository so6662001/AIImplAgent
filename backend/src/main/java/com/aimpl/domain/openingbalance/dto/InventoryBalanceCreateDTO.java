package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "货品ID不能为空")
    private Long productId;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    private Long locationId;

    @Size(max = 40, message = "批次号长度不能超过40")
    private String batchNo;

    private LocalDate inboundDate;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0", message = "数量不能为负数")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "重量不能为负数")
    private BigDecimal weight;

    @DecimalMin(value = "0.001", message = "件装量不能小于0.001")
    private BigDecimal packQuantity;

    @NotNull(message = "成本单价不能为空")
    @DecimalMin(value = "0", message = "成本单价不能为负数")
    private BigDecimal costUnitPrice;
}
