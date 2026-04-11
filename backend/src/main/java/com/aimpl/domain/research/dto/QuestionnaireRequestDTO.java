package com.aimpl.domain.research.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class QuestionnaireRequestDTO {

    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    @NotBlank(message = "企业规模不能为空")
    private String scale;

    private List<String> modules;
}
