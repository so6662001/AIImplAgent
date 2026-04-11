package com.aimpl.domain.course.vo;

import com.aimpl.domain.course.entity.CourseChapter;
import com.aimpl.domain.course.entity.LearningProgress;
import lombok.Data;

@Data
public class ChapterWithProgressVO {
    private Long id;
    private Long courseId;
    private Integer chapterNumber;
    private String chapterName;
    private String description;
    private String videoUrl;
    private Integer videoDuration;
    private Integer sortOrder;
    private String status;
    private Integer watchedDuration;

    public static ChapterWithProgressVO from(CourseChapter chapter, LearningProgress progress) {
        ChapterWithProgressVO vo = new ChapterWithProgressVO();
        vo.setId(chapter.getId());
        vo.setCourseId(chapter.getCourseId());
        vo.setChapterNumber(chapter.getChapterNumber());
        vo.setChapterName(chapter.getChapterName());
        vo.setDescription(chapter.getDescription());
        vo.setVideoUrl(chapter.getVideoUrl());
        vo.setVideoDuration(chapter.getVideoDuration());
        vo.setSortOrder(chapter.getSortOrder());
        if (progress != null) {
            vo.setStatus(progress.getStatus());
            vo.setWatchedDuration(progress.getWatchedDuration());
        } else {
            vo.setStatus("NOT_STARTED");
            vo.setWatchedDuration(0);
        }
        return vo;
    }
}
