package com.aimpl.domain.simulation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockTransactionVO {
    private String type;
    private String counterpartyName;
    private String productName;
    private BigDecimal qty;
    private BigDecimal weight;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private LocalDate date;
}
