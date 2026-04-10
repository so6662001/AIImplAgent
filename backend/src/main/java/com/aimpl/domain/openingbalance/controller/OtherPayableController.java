package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.OtherPayableCreateDTO;
import com.aimpl.domain.openingbalance.entity.OtherPayable;
import com.aimpl.domain.openingbalance.service.OtherPayableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/other-payables")
@RequiredArgsConstructor
public class OtherPayableController {

    private final OtherPayableService otherPayableService;

    @PostMapping
    public R<OtherPayable> create(@Valid @RequestBody OtherPayableCreateDTO dto) {
        return R.ok(otherPayableService.create(dto));
    }

    @GetMapping
    public R<List<OtherPayable>> list(@RequestParam Long projectId) {
        return R.ok(otherPayableService.listByProject(projectId));
    }
}
