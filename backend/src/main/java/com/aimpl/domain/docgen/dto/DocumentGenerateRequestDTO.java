package com.aimpl.domain.docgen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class DocumentGenerateRequestDTO {

    @NotBlank(message = "文档类型不能为空")
    private String documentType;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private Map<String, Object> params;
}
