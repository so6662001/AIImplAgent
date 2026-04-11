package com.aimpl.domain.course.vo;

import com.aimpl.domain.course.entity.Course;
import lombok.Data;

import java.util.List;

@Data
public class CourseDetailVO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private String industryType;
    private String module;
    private String coverImageUrl;
    private Integer totalChapters;
    private Integer totalDuration;
    private Integer completedChapters;
    private Integer progressPercent;
    private List<ChapterWithProgressVO> chapters;

    public static CourseDetailVO from(Course course) {
        CourseDetailVO vo = new CourseDetailVO();
        vo.setId(course.getId());
        vo.setCourseCode(course.getCourseCode());
        vo.setCourseName(course.getCourseName());
        vo.setDescription(course.getDescription());
        vo.setIndustryType(course.getIndustryType());
        vo.setModule(course.getModule());
        vo.setCoverImageUrl(course.getCoverImageUrl());
        vo.setTotalChapters(course.getTotalChapters());
        vo.setTotalDuration(course.getTotalDuration());
        return vo;
    }
}
