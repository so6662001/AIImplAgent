package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingDailyLogCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "日志日期不能为空")
    private LocalDate logDate;

    @NotBlank(message = "培训主题不能为空")
    @Size(max = 100, message = "培训主题长度不能超过100")
    private String topic;

    @NotBlank(message = "讲师姓名不能为空")
    private String trainerName;

    @Min(value = 0, message = "参训人数不能为负数")
    private Integer attendeeCount;

    private Boolean signInCompleted;
    private Boolean coursewareUploaded;
    private Boolean summaryUploaded;
    private Boolean examConducted;
    private Boolean dailyReportSubmitted;
    private String issues;
}
