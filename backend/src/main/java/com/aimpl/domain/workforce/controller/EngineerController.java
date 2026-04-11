package com.aimpl.domain.workforce.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.service.EngineerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engineers")
@RequiredArgsConstructor
public class EngineerController {

    private final EngineerService engineerService;

    @PostMapping
    public R<Engineer> create(@Valid @RequestBody EngineerCreateDTO dto) {
        return R.ok(engineerService.create(dto));
    }

    @GetMapping("/{id}")
    public R<Engineer> get(@PathVariable Long id) {
        return R.ok(engineerService.getById(id));
    }

    @GetMapping
    public R<List<Engineer>> list() {
        return R.ok(engineerService.list());
    }
}
