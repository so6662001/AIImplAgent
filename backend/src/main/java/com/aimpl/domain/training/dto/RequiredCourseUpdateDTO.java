package com.aimpl.domain.training.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequiredCourseUpdateDTO {

    @NotNull(message = "KA必学标记不能为空")
    private Boolean kaRequired;

    private Integer sortOrder;
}
