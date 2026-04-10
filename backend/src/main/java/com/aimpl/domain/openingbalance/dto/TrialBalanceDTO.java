package com.aimpl.domain.openingbalance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrialBalanceDTO {

    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private Boolean balanced;
}
