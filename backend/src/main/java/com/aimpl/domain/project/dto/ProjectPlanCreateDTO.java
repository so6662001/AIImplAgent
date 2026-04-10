package com.aimpl.domain.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectPlanCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 100, message = "计划名称长度不能超过100")
    private String planName;

    @Min(value = 1, message = "总天数不能小于1")
    private Integer totalDays;

    @NotBlank(message = "里程碑不能为空")
    private String milestones;

    private String wbsItems;

    private String resources;

    private String risks;

    private String status;
}
