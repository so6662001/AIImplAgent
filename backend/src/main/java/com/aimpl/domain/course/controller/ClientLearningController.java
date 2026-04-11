package com.aimpl.domain.course.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.course.dto.LearningProgressUpdateDTO;
import com.aimpl.domain.course.entity.LearningProgress;
import com.aimpl.domain.course.service.CourseService;
import com.aimpl.domain.course.service.LearningProgressService;
import com.aimpl.domain.course.vo.CourseDetailVO;
import com.aimpl.domain.course.vo.CourseWithProgressVO;
import com.aimpl.domain.course.vo.LearningProgressSummaryVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/learning")
@RequiredArgsConstructor
public class ClientLearningController {

    private final CourseService courseService;
    private final LearningProgressService learningProgressService;

    @GetMapping("/courses")
    public R<List<CourseWithProgressVO>> listCourses(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(courseService.listCoursesWithProgress(clientUserId));
    }

    @GetMapping("/courses/{courseId}")
    public R<CourseDetailVO> getCourseDetail(@PathVariable Long courseId, HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(courseService.getCourseDetail(courseId, clientUserId));
    }

    @PostMapping("/progress")
    public R<LearningProgress> updateProgress(@Valid @RequestBody LearningProgressUpdateDTO dto,
                                               HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        dto.setClientUserId(clientUserId);
        return R.ok(learningProgressService.updateProgress(dto));
    }

    @GetMapping("/summary")
    public R<LearningProgressSummaryVO> getProgressSummary(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(learningProgressService.getProgressSummary(clientUserId));
    }
}
