package com.aimpl.domain.dispatch.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispatchAssignDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "工程师ID不能为空")
    private Long engineerId;
}
