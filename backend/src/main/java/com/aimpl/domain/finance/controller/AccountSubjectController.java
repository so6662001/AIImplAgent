package com.aimpl.domain.finance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.finance.dto.AccountSubjectCreateDTO;
import com.aimpl.domain.finance.entity.AccountSubject;
import com.aimpl.domain.finance.service.AccountSubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-subjects")
@RequiredArgsConstructor
public class AccountSubjectController {

    private final AccountSubjectService accountSubjectService;

    @PostMapping
    public R<AccountSubject> create(@Valid @RequestBody AccountSubjectCreateDTO dto) {
        return R.ok(accountSubjectService.createSubject(dto));
    }

    @GetMapping("/{id}")
    public R<AccountSubject> get(@PathVariable Long id) {
        return R.ok(accountSubjectService.getById(id));
    }

    @GetMapping
    public R<List<AccountSubject>> list() {
        return R.ok(accountSubjectService.list());
    }
}
