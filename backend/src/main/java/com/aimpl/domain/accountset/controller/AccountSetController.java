package com.aimpl.domain.accountset.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.accountset.dto.AccountSetCreateDTO;
import com.aimpl.domain.accountset.entity.AccountSet;
import com.aimpl.domain.accountset.service.AccountSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-sets")
@RequiredArgsConstructor
public class AccountSetController {

    private final AccountSetService accountSetService;

    @PostMapping
    public R<AccountSet> create(@Valid @RequestBody AccountSetCreateDTO dto) {
        return R.ok(accountSetService.create(dto));
    }

    @GetMapping("/{id}")
    public R<AccountSet> get(@PathVariable Long id) {
        return R.ok(accountSetService.getById(id));
    }

    @GetMapping
    public R<List<AccountSet>> list(@RequestParam Long projectId) {
        return R.ok(accountSetService.listByProjectId(projectId));
    }

    @PutMapping("/{id}/activate")
    public R<AccountSet> activate(@PathVariable Long id) {
        return R.ok(accountSetService.activate(id));
    }
}
