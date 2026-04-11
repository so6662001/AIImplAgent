package com.aimpl.domain.simulation.vo;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedSceneVO {
    private Long id;
    private String sceneName;
    private String sceneType;
    private String description;
    private List<SceneStepVO> steps;
    private String expectedResult;
    private String difficulty;
}
