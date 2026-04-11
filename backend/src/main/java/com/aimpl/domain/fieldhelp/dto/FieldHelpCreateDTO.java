package com.aimpl.domain.fieldhelp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FieldHelpCreateDTO {

    @NotBlank(message = "页面不能为空")
    private String page;

    @NotBlank(message = "字段名不能为空")
    private String fieldName;

    @NotBlank(message = "帮助文本不能为空")
    private String helpText;

    private String formatExample;

    private String commonErrors;

    private Long relatedVideoClipId;

    private Integer sortOrder;
}
