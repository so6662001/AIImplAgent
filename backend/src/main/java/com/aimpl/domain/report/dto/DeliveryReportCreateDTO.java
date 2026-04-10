package com.aimpl.domain.report.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryReportCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "报告标题不能为空")
    @Size(max = 100, message = "报告标题长度不能超过100")
    private String title;

    @NotBlank(message = "报告类型不能为空")
    private String reportType;

    @DecimalMin(value = "0", message = "培训通过率不能为负数")
    @DecimalMax(value = "100", message = "培训通过率不能超过100")
    private BigDecimal trainingPassRate;

    @DecimalMin(value = "0", message = "数据导入完成率不能为负数")
    @DecimalMax(value = "100", message = "数据导入完成率不能超过100")
    private BigDecimal dataImportCompletionRate;

    private Integer totalIssues;

    private Integer resolvedIssues;

    @DecimalMin(value = "0", message = "客户满意度评分不能为负数")
    @DecimalMax(value = "5", message = "客户满意度评分不能超过5")
    private BigDecimal customerSatisfactionScore;

    private BigDecimal scheduleDeviationPercent;

    @DecimalMin(value = "0", message = "文档完成率不能为负数")
    @DecimalMax(value = "100", message = "文档完成率不能超过100")
    private BigDecimal documentCompletionRate;

    private String aiComment;
}
