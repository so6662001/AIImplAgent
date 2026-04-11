package com.aimpl.domain.aftersales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AfterSalesTicketCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "渠道不能为空")
    private String channel;

    @NotBlank(message = "意图类型不能为空")
    private String intentType;

    @Size(max = 30, message = "报告人姓名长度不能超过30")
    private String reporterName;

    @Size(max = 30, message = "报告人联系方式长度不能超过30")
    private String reporterContact;
}
