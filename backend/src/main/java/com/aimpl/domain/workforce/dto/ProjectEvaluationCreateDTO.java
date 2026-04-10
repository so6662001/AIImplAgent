package com.aimpl.domain.workforce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProjectEvaluationCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private Long evaluatorId;

    @NotNull(message = "进度评分不能为空")
    @Min(value = 0, message = "进度评分不能为负数")
    @Max(value = 100, message = "进度评分不能超过100")
    private Integer scheduleScore;

    @NotNull(message = "质量评分不能为空")
    @Min(value = 0, message = "质量评分不能为负数")
    @Max(value = 100, message = "质量评分不能超过100")
    private Integer qualityScore;

    @NotNull(message = "客户满意度评分不能为空")
    @Min(value = 0, message = "客户满意度评分不能为负数")
    @Max(value = 100, message = "客户满意度评分不能超过100")
    private Integer csatScore;

    @NotNull(message = "流程评分不能为空")
    @Min(value = 0, message = "流程评分不能为负数")
    @Max(value = 100, message = "流程评分不能超过100")
    private Integer processScore;

    @NotNull(message = "成本评分不能为空")
    @Min(value = 0, message = "成本评分不能为负数")
    @Max(value = 100, message = "成本评分不能超过100")
    private Integer costScore;

    private String aiComment;
}
