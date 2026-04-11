package com.aimpl.domain.onlineexam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamPaperCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "考核模块不能为空")
    private String module;

    @NotBlank(message = "难度级别不能为空")
    private String difficulty;

    private Long createdFor;
}
