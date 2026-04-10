package com.aimpl.domain.training.vo;

import lombok.Data;

@Data
public class WeakModuleVO {
    private String module;
    private int bestScore;
    private int attempts;
    private String status;
}
