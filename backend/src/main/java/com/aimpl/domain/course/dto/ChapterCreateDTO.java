package com.aimpl.domain.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChapterCreateDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Min(value = 1, message = "章节序号至少为1")
    private Integer chapterNumber;

    @NotBlank(message = "章节名称不能为空")
    @Size(max = 100, message = "章节名称最长100个字符")
    private String chapterName;

    private String description;

    @NotBlank(message = "视频URL不能为空")
    private String videoUrl;

    @Min(value = 1, message = "视频时长至少为1秒")
    private Integer videoDuration;

    private Integer sortOrder;
}
