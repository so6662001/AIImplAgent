package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.InventoryBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.InventoryBalance;
import com.aimpl.domain.openingbalance.service.InventoryBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory-balances")
@RequiredArgsConstructor
public class InventoryBalanceController {

    private final InventoryBalanceService inventoryBalanceService;

    @PostMapping
    public R<InventoryBalance> create(@Valid @RequestBody InventoryBalanceCreateDTO dto) {
        return R.ok(inventoryBalanceService.create(dto));
    }

    @GetMapping
    public R<List<InventoryBalance>> list(@RequestParam Long projectId) {
        return R.ok(inventoryBalanceService.listByProject(projectId));
    }
}
