package com.aimpl.domain.simulation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneStepVO {
    private int stepNumber;
    private String instruction;
    private String expectedAction;
    private String checkpoint;
}
