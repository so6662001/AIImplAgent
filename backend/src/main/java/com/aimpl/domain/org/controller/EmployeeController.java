package com.aimpl.domain.org.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.org.dto.EmployeeCreateDTO;
import com.aimpl.domain.org.entity.Employee;
import com.aimpl.domain.org.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public R<Employee> create(@Valid @RequestBody EmployeeCreateDTO dto) {
        return R.ok(employeeService.createEmployee(dto));
    }

    @GetMapping("/{id}")
    public R<Employee> get(@PathVariable Long id) {
        return R.ok(employeeService.getById(id));
    }

    @GetMapping
    public R<List<Employee>> list() {
        return R.ok(employeeService.list());
    }
}
