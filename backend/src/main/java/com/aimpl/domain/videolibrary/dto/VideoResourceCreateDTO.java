package com.aimpl.domain.videolibrary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoResourceCreateDTO {

    @NotBlank(message = "模块不能为空")
    private String module;

    @NotBlank(message = "功能名称不能为空")
    private String functionName;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "视频地址不能为空")
    private String videoUrl;

    @Min(value = 1, message = "视频时长不能小于1秒")
    private Integer duration;

    private Integer sortOrder;
}
