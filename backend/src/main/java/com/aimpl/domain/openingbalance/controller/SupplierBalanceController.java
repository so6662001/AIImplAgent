package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.SupplierBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.SupplierBalance;
import com.aimpl.domain.openingbalance.service.SupplierBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-balances")
@RequiredArgsConstructor
public class SupplierBalanceController {

    private final SupplierBalanceService supplierBalanceService;

    @PostMapping
    public R<SupplierBalance> create(@Valid @RequestBody SupplierBalanceCreateDTO dto) {
        return R.ok(supplierBalanceService.create(dto));
    }

    @GetMapping
    public R<List<SupplierBalance>> list(@RequestParam Long projectId) {
        return R.ok(supplierBalanceService.listByProject(projectId));
    }
}
