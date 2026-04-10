package com.aimpl.domain.project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProjectPlanCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100, message = "计划名称长度不能超过100")
    private String planName;

    @Min(value = 1, message = "总天数不能小于1")
    @Max(value = 3650, message = "总天数不能超过3650")
    private Integer totalDays;

    @NotBlank(message = "里程碑不能为空")
    @Size(max = 65535, message = "里程碑内容过长")
    private String milestones;

    @Size(max = 65535, message = "WBS条目内容过长")
    private String wbsItems;

    @Size(max = 65535, message = "资源内容过长")
    private String resources;

    @Size(max = 65535, message = "风险内容过长")
    private String risks;

    @Size(max = 20, message = "状态长度不能超过20")
    private String status;
}
