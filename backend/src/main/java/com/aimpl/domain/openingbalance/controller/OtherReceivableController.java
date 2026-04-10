package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.OtherReceivableCreateDTO;
import com.aimpl.domain.openingbalance.entity.OtherReceivable;
import com.aimpl.domain.openingbalance.service.OtherReceivableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/other-receivables")
@RequiredArgsConstructor
public class OtherReceivableController {

    private final OtherReceivableService otherReceivableService;

    @PostMapping
    public R<OtherReceivable> create(@Valid @RequestBody OtherReceivableCreateDTO dto) {
        return R.ok(otherReceivableService.create(dto));
    }

    @GetMapping
    public R<List<OtherReceivable>> list(@RequestParam Long projectId) {
        return R.ok(otherReceivableService.listByProject(projectId));
    }
}
