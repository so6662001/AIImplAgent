package com.aimpl.domain.training.vo;

import lombok.Data;

import java.util.List;

@Data
public class WeakPointAnalysisVO {
    private Long traineeId;
    private String traineeName;
    private int totalModules;
    private int passedModules;
    private List<WeakModuleVO> weakModules;
    private List<RemediationItemVO> remediationPlan;
}
