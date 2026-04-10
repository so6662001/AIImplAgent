package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BatchToggleDTO {

    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    @NotEmpty(message = "课程模块列表不能为空")
    private List<String> courseModules;

    @NotNull(message = "必学标记不能为空")
    private Boolean kaRequired;
}
