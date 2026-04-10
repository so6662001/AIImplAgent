package com.aimpl.domain.simulation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SimulationSceneCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "场景名称不能为空")
    @Size(max = 100, message = "场景名称长度不能超过100")
    private String sceneName;

    @NotBlank(message = "场景类型不能为空")
    private String sceneType;

    private String description;

    @NotBlank(message = "操作步骤不能为空")
    private String steps;

    private String expectedResult;
}
