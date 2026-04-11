package com.aimpl.domain.course.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.course.dto.LearningProgressUpdateDTO;
import com.aimpl.domain.course.entity.Course;
import com.aimpl.domain.course.entity.CourseChapter;
import com.aimpl.domain.course.entity.LearningProgress;
import com.aimpl.domain.course.mapper.CourseChapterMapper;
import com.aimpl.domain.course.mapper.CourseMapper;
import com.aimpl.domain.course.mapper.LearningProgressMapper;
import com.aimpl.domain.course.vo.LearningProgressSummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningProgressService extends ServiceImpl<LearningProgressMapper, LearningProgress> {

    private final CourseChapterMapper chapterMapper;
    private final CourseMapper courseMapper;

    @Transactional
    public LearningProgress updateProgress(LearningProgressUpdateDTO dto) {
        CourseChapter chapter = chapterMapper.selectById(dto.getChapterId());
        if (chapter == null) {
            throw new BizException("章节不存在: " + dto.getChapterId());
        }

        LearningProgress progress = getOne(new LambdaQueryWrapper<LearningProgress>()
                .eq(LearningProgress::getClientUserId, dto.getClientUserId())
                .eq(LearningProgress::getChapterId, dto.getChapterId()));

        if (progress == null) {
            progress = new LearningProgress();
            progress.setClientUserId(dto.getClientUserId());
            progress.setCourseId(chapter.getCourseId());
            progress.setChapterId(dto.getChapterId());
            progress.setWatchedDuration(dto.getWatchedDuration() != null ? dto.getWatchedDuration() : 0);
            progress.setStatus(dto.getStatus());

            if (shouldAutoComplete(progress.getWatchedDuration(), chapter.getVideoDuration())) {
                progress.setStatus("COMPLETED");
                progress.setCompletedAt(LocalDateTime.now());
            }

            save(progress);
        } else {
            if (dto.getWatchedDuration() != null) {
                progress.setWatchedDuration(dto.getWatchedDuration());
            }
            progress.setStatus(dto.getStatus());

            if (shouldAutoComplete(progress.getWatchedDuration(), chapter.getVideoDuration())) {
                progress.setStatus("COMPLETED");
                if (progress.getCompletedAt() == null) {
                    progress.setCompletedAt(LocalDateTime.now());
                }
            }

            updateById(progress);
        }
        return progress;
    }

    private boolean shouldAutoComplete(Integer watchedDuration, Integer videoDuration) {
        if (watchedDuration == null || videoDuration == null || videoDuration <= 0) {
            return false;
        }
        return watchedDuration >= videoDuration * 0.9;
    }

    public LearningProgressSummaryVO getProgressSummary(Long clientUserId) {
        List<LearningProgress> allProgress = list(new LambdaQueryWrapper<LearningProgress>()
                .eq(LearningProgress::getClientUserId, clientUserId));

        List<Course> allCourses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getEnabled, true));

        Map<Long, List<LearningProgress>> byCourse = allProgress.stream()
                .collect(Collectors.groupingBy(LearningProgress::getCourseId));

        int completedCourses = 0;
        int inProgressCourses = 0;

        for (Course course : allCourses) {
            List<LearningProgress> courseProgress = byCourse.get(course.getId());
            if (courseProgress == null || courseProgress.isEmpty()) {
                continue;
            }

            long completedChapters = courseProgress.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()))
                    .count();

            int totalChapters = course.getTotalChapters() != null ? course.getTotalChapters() : 0;
            if (totalChapters > 0 && completedChapters >= totalChapters) {
                completedCourses++;
            } else if (!courseProgress.isEmpty()) {
                inProgressCourses++;
            }
        }

        int totalWatchTime = allProgress.stream()
                .mapToInt(p -> p.getWatchedDuration() != null ? p.getWatchedDuration() : 0)
                .sum();

        LearningProgressSummaryVO summary = new LearningProgressSummaryVO();
        summary.setTotalCourses(allCourses.size());
        summary.setCompletedCourses(completedCourses);
        summary.setInProgressCourses(inProgressCourses);
        summary.setTotalWatchTime(totalWatchTime);
        return summary;
    }
}
