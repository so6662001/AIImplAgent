package com.aimpl.domain.training.vo;

import lombok.Data;

@Data
public class TraineeStatusVO {
    private Long id;
    private String name;
    private String role;
    private boolean kaUser;
    private int progressPercent;
    private int attendanceDays;
    private int totalDays;
    private int passedModules;
    private int totalModules;
    private String riskLevel;
}
