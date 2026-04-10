package com.aimpl.domain.finance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.finance.dto.BankAccountCreateDTO;
import com.aimpl.domain.finance.entity.BankAccount;
import com.aimpl.domain.finance.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public R<BankAccount> create(@Valid @RequestBody BankAccountCreateDTO dto) {
        return R.ok(bankAccountService.createBankAccount(dto));
    }

    @GetMapping("/{id}")
    public R<BankAccount> get(@PathVariable Long id) {
        return R.ok(bankAccountService.getById(id));
    }

    @GetMapping
    public R<List<BankAccount>> list() {
        return R.ok(bankAccountService.list());
    }
}
