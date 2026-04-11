package com.aimpl.domain.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseCreateDTO {

    @NotBlank(message = "课程编码不能为空")
    @Size(max = 20, message = "课程编码最长20个字符")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称最长100个字符")
    private String courseName;

    private String description;

    private String industryType;

    private String module;

    private String coverImageUrl;

    @Min(value = 1, message = "总章节数至少为1")
    private Integer totalChapters;

    @Min(value = 0, message = "总时长不能为负")
    private Integer totalDuration;

    private Integer sortOrder;
}
