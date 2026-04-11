package com.aimpl.domain.course.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.course.dto.ChapterCreateDTO;
import com.aimpl.domain.course.dto.CourseCreateDTO;
import com.aimpl.domain.course.entity.Course;
import com.aimpl.domain.course.entity.CourseChapter;
import com.aimpl.domain.course.entity.LearningProgress;
import com.aimpl.domain.course.mapper.CourseChapterMapper;
import com.aimpl.domain.course.mapper.CourseMapper;
import com.aimpl.domain.course.mapper.LearningProgressMapper;
import com.aimpl.domain.course.vo.ChapterWithProgressVO;
import com.aimpl.domain.course.vo.CourseDetailVO;
import com.aimpl.domain.course.vo.CourseWithProgressVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    private final CourseChapterMapper chapterMapper;
    private final LearningProgressMapper progressMapper;

    @Transactional
    public Course createCourse(CourseCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, dto.getCourseCode()));
        if (count > 0) {
            throw new BizException("课程编码已存在: " + dto.getCourseCode());
        }

        Course course = new Course();
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setIndustryType(dto.getIndustryType());
        course.setModule(dto.getModule());
        course.setCoverImageUrl(dto.getCoverImageUrl());
        course.setTotalChapters(dto.getTotalChapters() != null ? dto.getTotalChapters() : 0);
        course.setTotalDuration(dto.getTotalDuration() != null ? dto.getTotalDuration() : 0);
        course.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        course.setEnabled(true);
        save(course);
        return course;
    }

    public List<Course> listCourses() {
        return list(new LambdaQueryWrapper<Course>()
                .eq(Course::getEnabled, true)
                .orderByAsc(Course::getSortOrder));
    }

    public Course getCourseById(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BizException("课程不存在: " + id);
        }
        return course;
    }

    @Transactional
    public CourseChapter addChapter(Long courseId, ChapterCreateDTO dto) {
        Course course = getById(courseId);
        if (course == null) {
            throw new BizException("课程不存在: " + courseId);
        }

        long dupCount = chapterMapper.selectCount(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .eq(CourseChapter::getChapterNumber, dto.getChapterNumber()));
        if (dupCount > 0) {
            throw new BizException("章节序号已存在: " + dto.getChapterNumber());
        }

        CourseChapter chapter = new CourseChapter();
        chapter.setCourseId(courseId);
        chapter.setChapterNumber(dto.getChapterNumber());
        chapter.setChapterName(dto.getChapterName());
        chapter.setDescription(dto.getDescription());
        chapter.setVideoUrl(dto.getVideoUrl());
        chapter.setVideoDuration(dto.getVideoDuration());
        chapter.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : dto.getChapterNumber());
        chapter.setEnabled(true);
        chapterMapper.insert(chapter);
        return chapter;
    }

    public List<CourseChapter> listChapters(Long courseId) {
        return chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .eq(CourseChapter::getEnabled, true)
                .orderByAsc(CourseChapter::getSortOrder));
    }

    public List<CourseWithProgressVO> listCoursesWithProgress(Long clientUserId) {
        List<Course> courses = listCourses();
        List<LearningProgress> allProgress = progressMapper.selectList(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getClientUserId, clientUserId)
                        .eq(LearningProgress::getStatus, "COMPLETED"));

        Map<Long, Long> completedByCourse = allProgress.stream()
                .collect(Collectors.groupingBy(LearningProgress::getCourseId, Collectors.counting()));

        List<CourseWithProgressVO> result = new ArrayList<>();
        for (Course course : courses) {
            int completed = completedByCourse.getOrDefault(course.getId(), 0L).intValue();
            result.add(CourseWithProgressVO.from(course, completed));
        }
        return result;
    }

    public CourseDetailVO getCourseDetail(Long courseId, Long clientUserId) {
        Course course = getCourseById(courseId);
        List<CourseChapter> chapters = listChapters(courseId);

        Map<Long, LearningProgress> progressMap = Map.of();
        if (clientUserId != null) {
            List<LearningProgress> progressList = progressMapper.selectList(
                    new LambdaQueryWrapper<LearningProgress>()
                            .eq(LearningProgress::getClientUserId, clientUserId)
                            .eq(LearningProgress::getCourseId, courseId));
            progressMap = progressList.stream()
                    .collect(Collectors.toMap(LearningProgress::getChapterId, p -> p, (a, b) -> b));
        }

        CourseDetailVO detail = CourseDetailVO.from(course);
        List<ChapterWithProgressVO> chapterVOs = new ArrayList<>();
        int completedCount = 0;
        for (CourseChapter ch : chapters) {
            LearningProgress progress = progressMap.get(ch.getId());
            chapterVOs.add(ChapterWithProgressVO.from(ch, progress));
            if (progress != null && "COMPLETED".equals(progress.getStatus())) {
                completedCount++;
            }
        }
        detail.setChapters(chapterVOs);
        detail.setCompletedChapters(completedCount);
        int total = course.getTotalChapters() != null && course.getTotalChapters() > 0 ? course.getTotalChapters() : 1;
        detail.setProgressPercent(completedCount * 100 / total);
        return detail;
    }
}
