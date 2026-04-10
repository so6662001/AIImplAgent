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
    @Size(max = 30, message = "场景类型长度不能超过30")
    private String sceneType;

    @Size(max = 1000, message = "描述长度不能超过1000")
    private String description;

    @NotBlank(message = "操作步骤不能为空")
    @Size(max = 4000, message = "操作步骤长度不能超过4000")
    private String steps;

    @Size(max = 2000, message = "预期结果长度不能超过2000")
    private String expectedResult;
}
