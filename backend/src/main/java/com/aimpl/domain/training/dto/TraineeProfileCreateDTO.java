package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TraineeProfileCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "员工姓名不能为空")
    @Size(max = 30, message = "员工姓名长度不能超过30")
    private String employeeName;

    @NotBlank(message = "角色不能为空")
    @Size(max = 30, message = "角色长度不能超过30")
    private String role;

    @Size(max = 30, message = "部门长度不能超过30")
    private String department;

    private Boolean kaUser = false;
}
