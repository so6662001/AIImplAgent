package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.WarehouseCreateDTO;
import com.aimpl.domain.archive.entity.Warehouse;
import com.aimpl.domain.archive.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public R<Warehouse> create(@Valid @RequestBody WarehouseCreateDTO dto) {
        return R.ok(warehouseService.createWarehouse(dto));
    }

    @GetMapping("/{id}")
    public R<Warehouse> get(@PathVariable Long id) {
        return R.ok(warehouseService.getById(id));
    }

    @GetMapping
    public R<List<Warehouse>> list() {
        return R.ok(warehouseService.list());
    }
}
