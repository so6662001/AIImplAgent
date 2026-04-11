package com.aimpl.domain.course.vo;

import lombok.Data;

@Data
public class LearningProgressSummaryVO {
    private int totalCourses;
    private int completedCourses;
    private int inProgressCourses;
    private int totalWatchTime;
}
