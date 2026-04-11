package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ExamRecordCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotNull(message = "学员ID不能为空")
    private Long traineeId;

    @NotBlank(message = "考核模块不能为空")
    @Size(max = 50, message = "考核模块名称长度不能超过50")
    private String module;

    @NotBlank(message = "考核类型不能为空")
    @Size(max = 20, message = "考核类型长度不能超过20")
    private String examType;

    @NotNull(message = "考核分数不能为空")
    @Min(value = 0, message = "考核分数不能为负数")
    @Max(value = 100, message = "考核分数不能超过100")
    private Integer score;

    private Boolean requiredCourse;

    @Size(max = 500, message = "薄弱点长度不能超过500")
    private String weakPoints;

    private Long retryOf;
}
