package com.aimpl.domain.simulation.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SimulationExecuteDTO {

    @NotBlank(message = "实际结果不能为空")
    @Size(max = 2000, message = "实际结果长度不能超过2000")
    private String actualResult;

    @NotBlank(message = "执行状态不能为空")
    @Size(max = 20, message = "执行状态长度不能超过20")
    private String status;

    @Size(max = 60, message = "执行人长度不能超过60")
    private String executedBy;

    @Size(max = 2000, message = "偏差描述长度不能超过2000")
    private String deviation;
}
