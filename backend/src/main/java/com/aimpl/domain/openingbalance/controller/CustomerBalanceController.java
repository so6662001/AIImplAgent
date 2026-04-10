package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.CustomerBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.CustomerBalance;
import com.aimpl.domain.openingbalance.service.CustomerBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-balances")
@RequiredArgsConstructor
public class CustomerBalanceController {

    private final CustomerBalanceService customerBalanceService;

    @PostMapping
    public R<CustomerBalance> create(@Valid @RequestBody CustomerBalanceCreateDTO dto) {
        return R.ok(customerBalanceService.create(dto));
    }

    @GetMapping
    public R<List<CustomerBalance>> list(@RequestParam Long projectId) {
        return R.ok(customerBalanceService.listByProject(projectId));
    }
}
