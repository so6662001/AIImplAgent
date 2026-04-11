package com.aimpl.domain.uservideo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoLearningDTO {

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    @Min(value = 0, message = "观看时长不能为负")
    private Integer watchedDuration;
}
