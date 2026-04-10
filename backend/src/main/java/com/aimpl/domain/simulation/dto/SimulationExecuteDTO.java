package com.aimpl.domain.simulation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SimulationExecuteDTO {

    @NotBlank(message = "实际结果不能为空")
    private String actualResult;

    @NotBlank(message = "执行状态不能为空")
    private String status;

    private String executedBy;

    private String deviation;
}
