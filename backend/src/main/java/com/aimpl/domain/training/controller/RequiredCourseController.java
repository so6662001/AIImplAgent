package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.BatchToggleDTO;
import com.aimpl.domain.training.dto.RequiredCourseCreateDTO;
import com.aimpl.domain.training.dto.RequiredCourseUpdateDTO;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.service.RequiredCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/required-courses")
@RequiredArgsConstructor
public class RequiredCourseController {

    private final RequiredCourseService requiredCourseService;

    @GetMapping
    public R<List<RequiredCourse>> list(
            @RequestParam(required = false) String industryType) {
        return R.ok(requiredCourseService.listByIndustryType(industryType));
    }

    @PostMapping
    public R<RequiredCourse> create(@Valid @RequestBody RequiredCourseCreateDTO dto) {
        return R.ok(requiredCourseService.createCourse(dto));
    }

    @PutMapping("/{id}")
    public R<RequiredCourse> update(@PathVariable Long id,
                                    @Valid @RequestBody RequiredCourseUpdateDTO dto) {
        return R.ok(requiredCourseService.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        requiredCourseService.deleteCourse(id);
        return R.ok();
    }

    @PostMapping("/batch-toggle")
    public R<Void> batchToggle(@Valid @RequestBody BatchToggleDTO dto) {
        requiredCourseService.batchToggle(
                dto.getIndustryType(), dto.getCourseModules(), dto.getKaRequired());
        return R.ok();
    }
}
