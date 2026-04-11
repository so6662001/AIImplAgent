package com.aimpl.domain.training.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ModuleTrainingStatusVO {
    private String module;
    private boolean trained;
    private int examCount;
    private BigDecimal passRate;
    private String topWeakPoint;
}
