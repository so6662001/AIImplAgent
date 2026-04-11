package com.aimpl.domain.videolibrary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoClipCreateDTO {

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    @NotBlank(message = "片段标题不能为空")
    private String clipTitle;

    @Min(value = 0, message = "开始时间不能为负")
    private Integer startSecond;

    @Min(value = 1, message = "结束时间不能小于1秒")
    private Integer endSecond;

    private String relatedPage;

    private String relatedField;

    private String operationStep;

    private String subtitleText;

    private Integer sortOrder;
}
