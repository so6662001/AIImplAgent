package com.aimpl.domain.course.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.course.dto.ChapterCreateDTO;
import com.aimpl.domain.course.dto.CourseCreateDTO;
import com.aimpl.domain.course.entity.Course;
import com.aimpl.domain.course.entity.CourseChapter;
import com.aimpl.domain.course.service.CourseService;
import com.aimpl.domain.course.vo.CourseDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public R<Course> createCourse(@Valid @RequestBody CourseCreateDTO dto) {
        return R.ok(courseService.createCourse(dto));
    }

    @GetMapping
    public R<List<Course>> listCourses() {
        return R.ok(courseService.listCourses());
    }

    @GetMapping("/{id}")
    public R<CourseDetailVO> getCourse(@PathVariable Long id) {
        return R.ok(courseService.getCourseDetail(id, null));
    }

    @PostMapping("/{courseId}/chapters")
    public R<CourseChapter> addChapter(@PathVariable Long courseId,
                                       @Valid @RequestBody ChapterCreateDTO dto) {
        dto.setCourseId(courseId);
        return R.ok(courseService.addChapter(courseId, dto));
    }

    @GetMapping("/{courseId}/chapters")
    public R<List<CourseChapter>> listChapters(@PathVariable Long courseId) {
        return R.ok(courseService.listChapters(courseId));
    }
}
