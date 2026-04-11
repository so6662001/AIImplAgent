package com.aimpl.domain.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LearningProgressUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long clientUserId;

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @Min(value = 0, message = "观看时长不能为负")
    private Integer watchedDuration;

    @NotBlank(message = "状态不能为空")
    private String status;
}
