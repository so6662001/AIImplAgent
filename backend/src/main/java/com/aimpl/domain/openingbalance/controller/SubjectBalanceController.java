package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.SubjectBalanceCreateDTO;
import com.aimpl.domain.openingbalance.dto.TrialBalanceDTO;
import com.aimpl.domain.openingbalance.entity.SubjectBalance;
import com.aimpl.domain.openingbalance.service.SubjectBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subject-balances")
@RequiredArgsConstructor
public class SubjectBalanceController {

    private final SubjectBalanceService subjectBalanceService;

    @PostMapping
    public R<SubjectBalance> create(@Valid @RequestBody SubjectBalanceCreateDTO dto) {
        return R.ok(subjectBalanceService.create(dto));
    }

    @GetMapping
    public R<List<SubjectBalance>> list(@RequestParam Long projectId) {
        return R.ok(subjectBalanceService.listByProject(projectId));
    }

    @GetMapping("/trial-balance/{projectId}")
    public R<TrialBalanceDTO> trialBalance(@PathVariable Long projectId) {
        return R.ok(subjectBalanceService.getTrialBalance(projectId));
    }
}
