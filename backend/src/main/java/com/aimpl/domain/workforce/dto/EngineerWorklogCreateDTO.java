package com.aimpl.domain.workforce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EngineerWorklogCreateDTO {

    @NotNull(message = "工程师ID不能为空")
    private Long engineerId;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "工作日期不能为空")
    private LocalDate workDate;

    @NotBlank(message = "计划任务不能为空")
    private String tasksPlan;

    private String tasksCompleted;

    private String documentsSubmitted;

    private String issues;

    private String nextDayPlan;
}
