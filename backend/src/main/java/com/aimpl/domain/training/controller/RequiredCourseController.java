package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.service.RequiredCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/required-courses")
@RequiredArgsConstructor
public class RequiredCourseController {

    private final RequiredCourseService requiredCourseService;

    @GetMapping
    public R<List<RequiredCourse>> list(@RequestParam String industryType) {
        return R.ok(requiredCourseService.listByIndustryType(industryType));
    }
}
