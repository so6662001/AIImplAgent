package com.aimpl.domain.uservideo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserVideoPublishDTO {

    @NotBlank(message = "视频标题不能为空")
    @Size(max = 100, message = "视频标题最长100个字符")
    private String title;

    private String description;

    @NotBlank(message = "分类模块不能为空")
    private String categoryModule;

    @NotBlank(message = "视频地址不能为空")
    private String videoUrl;

    @Min(value = 1, message = "视频时长至少为1秒")
    private Integer videoDuration;

    @Min(value = 0, message = "积分费用不能为负")
    @Max(value = 100, message = "积分费用最大为100")
    private Integer pointsCost;

    private String thumbnailUrl;
}
