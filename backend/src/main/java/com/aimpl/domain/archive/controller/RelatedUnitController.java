package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.RelatedUnitCreateDTO;
import com.aimpl.domain.archive.entity.RelatedUnit;
import com.aimpl.domain.archive.service.RelatedUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/related-units")
@RequiredArgsConstructor
public class RelatedUnitController {

    private final RelatedUnitService relatedUnitService;

    @PostMapping
    public R<RelatedUnit> create(@Valid @RequestBody RelatedUnitCreateDTO dto) {
        return R.ok(relatedUnitService.createRelatedUnit(dto));
    }

    @GetMapping("/{id}")
    public R<RelatedUnit> get(@PathVariable Long id) {
        return R.ok(relatedUnitService.getById(id));
    }

    @GetMapping
    public R<List<RelatedUnit>> list() {
        return R.ok(relatedUnitService.list());
    }
}
