package com.aimpl.domain.simulation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockInventoryVO {
    private String productName;
    private String spec;
    private String warehouse;
    private BigDecimal qty;
    private BigDecimal weight;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
