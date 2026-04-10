package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequiredCourseCreateDTO {

    @NotBlank(message = "行业类型不能为空")
    private String industryType;

    @NotBlank(message = "课程模块不能为空")
    @Size(max = 50, message = "课程模块名称长度不能超过50")
    private String courseModule;

    private Boolean kaRequired;

    private Integer sortOrder;
}
