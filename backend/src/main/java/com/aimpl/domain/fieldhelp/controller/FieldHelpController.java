package com.aimpl.domain.fieldhelp.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.fieldhelp.dto.FieldHelpCreateDTO;
import com.aimpl.domain.fieldhelp.entity.FieldHelpContent;
import com.aimpl.domain.fieldhelp.service.FieldHelpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field-help")
@RequiredArgsConstructor
public class FieldHelpController {

    private final FieldHelpService fieldHelpService;

    @PostMapping
    public R<FieldHelpContent> create(@Valid @RequestBody FieldHelpCreateDTO dto) {
        return R.ok(fieldHelpService.createHelp(dto));
    }

    @GetMapping
    public R<List<FieldHelpContent>> listByPage(@RequestParam String page) {
        return R.ok(fieldHelpService.getPageHelps(page));
    }

    @GetMapping("/lookup")
    public R<FieldHelpContent> lookup(@RequestParam String page, @RequestParam String field) {
        return R.ok(fieldHelpService.getHelp(page, field));
    }
}
