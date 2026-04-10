package com.aimpl.domain.org.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.org.dto.DepartmentCreateDTO;
import com.aimpl.domain.org.entity.Department;
import com.aimpl.domain.org.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public R<Department> create(@Valid @RequestBody DepartmentCreateDTO dto) {
        return R.ok(departmentService.createDepartment(dto));
    }

    @GetMapping("/{id}")
    public R<Department> get(@PathVariable Long id) {
        return R.ok(departmentService.getById(id));
    }

    @GetMapping
    public R<List<Department>> list() {
        return R.ok(departmentService.list());
    }
}
