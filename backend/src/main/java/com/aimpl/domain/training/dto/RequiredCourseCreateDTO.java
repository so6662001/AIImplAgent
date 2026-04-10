package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RequiredCourseCreateDTO {

    @NotBlank(message = "行业类型不能为空")
    @Size(max = 30, message = "行业类型长度不能超过30")
    private String industryType;

    @NotBlank(message = "课程模块不能为空")
    @Size(max = 50, message = "课程模块名称长度不能超过50")
    private String courseModule;

    private Boolean kaRequired;

    @Min(value = 0, message = "排序值不能为负数")
    @Max(value = 999999, message = "排序值过大")
    private Integer sortOrder;
}
