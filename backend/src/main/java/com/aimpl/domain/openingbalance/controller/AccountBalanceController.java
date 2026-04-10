package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.AccountBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.AccountBalance;
import com.aimpl.domain.openingbalance.service.AccountBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account-balances")
@RequiredArgsConstructor
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;

    @PostMapping
    public R<AccountBalance> create(@Valid @RequestBody AccountBalanceCreateDTO dto) {
        return R.ok(accountBalanceService.create(dto));
    }

    @GetMapping
    public R<List<AccountBalance>> list(@RequestParam Long projectId) {
        return R.ok(accountBalanceService.listByProject(projectId));
    }
}
