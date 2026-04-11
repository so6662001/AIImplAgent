package com.aimpl.domain.course.vo;

import com.aimpl.domain.course.entity.Course;
import lombok.Data;

@Data
public class CourseWithProgressVO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private String industryType;
    private String module;
    private String coverImageUrl;
    private Integer totalChapters;
    private Integer totalDuration;
    private Integer sortOrder;
    private Integer completedChapters;
    private Integer progressPercent;

    public static CourseWithProgressVO from(Course course, int completedChapters) {
        CourseWithProgressVO vo = new CourseWithProgressVO();
        vo.setId(course.getId());
        vo.setCourseCode(course.getCourseCode());
        vo.setCourseName(course.getCourseName());
        vo.setDescription(course.getDescription());
        vo.setIndustryType(course.getIndustryType());
        vo.setModule(course.getModule());
        vo.setCoverImageUrl(course.getCoverImageUrl());
        vo.setTotalChapters(course.getTotalChapters());
        vo.setTotalDuration(course.getTotalDuration());
        vo.setSortOrder(course.getSortOrder());
        vo.setCompletedChapters(completedChapters);
        int total = course.getTotalChapters() != null && course.getTotalChapters() > 0 ? course.getTotalChapters() : 1;
        vo.setProgressPercent(completedChapters * 100 / total);
        return vo;
    }
}
