package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RequiredCourseUpdateDTO {

    @NotNull(message = "KA必学标记不能为空")
    private Boolean kaRequired;

    @Min(value = 0, message = "排序值不能为负数")
    @Max(value = 999999, message = "排序值过大")
    private Integer sortOrder;
}
