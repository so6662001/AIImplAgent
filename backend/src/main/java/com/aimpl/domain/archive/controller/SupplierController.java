package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.SupplierCreateDTO;
import com.aimpl.domain.archive.entity.Supplier;
import com.aimpl.domain.archive.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public R<Supplier> create(@Valid @RequestBody SupplierCreateDTO dto) {
        return R.ok(supplierService.createSupplier(dto));
    }

    @GetMapping("/{id}")
    public R<Supplier> get(@PathVariable Long id) {
        return R.ok(supplierService.getById(id));
    }

    @GetMapping
    public R<List<Supplier>> list() {
        return R.ok(supplierService.list());
    }
}
