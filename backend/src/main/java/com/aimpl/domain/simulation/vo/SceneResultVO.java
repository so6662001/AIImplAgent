package com.aimpl.domain.simulation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SceneResultVO {
    private String sceneName;
    private String sceneType;
    private String status;
    private BigDecimal score;
    private String deviation;
    private String recommendation;
}
