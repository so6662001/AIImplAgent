package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.StorageLocationCreateDTO;
import com.aimpl.domain.archive.entity.StorageLocation;
import com.aimpl.domain.archive.service.StorageLocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage-locations")
@RequiredArgsConstructor
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    @PostMapping
    public R<StorageLocation> create(@Valid @RequestBody StorageLocationCreateDTO dto) {
        return R.ok(storageLocationService.createLocation(dto));
    }

    @GetMapping("/{id}")
    public R<StorageLocation> get(@PathVariable Long id) {
        return R.ok(storageLocationService.getById(id));
    }

    @GetMapping
    public R<List<StorageLocation>> list(@RequestParam(required = false) Long warehouseId) {
        if (warehouseId != null) {
            return R.ok(storageLocationService.listByWarehouseId(warehouseId));
        }
        return R.ok(storageLocationService.list());
    }
}
