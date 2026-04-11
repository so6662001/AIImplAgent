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
    @Size(max = 2000, message = "计划任务长度不能超过2000")
    private String tasksPlan;

    @Size(max = 2000, message = "完成任务长度不能超过2000")
    private String tasksCompleted;

    @Size(max = 2000, message = "提交文档长度不能超过2000")
    private String documentsSubmitted;

    @Size(max = 2000, message = "问题描述长度不能超过2000")
    private String issues;

    @Size(max = 2000, message = "次日计划长度不能超过2000")
    private String nextDayPlan;
}
