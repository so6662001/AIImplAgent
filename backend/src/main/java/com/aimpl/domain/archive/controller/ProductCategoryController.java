package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.ProductCategoryCreateDTO;
import com.aimpl.domain.archive.entity.ProductCategory;
import com.aimpl.domain.archive.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping
    public R<ProductCategory> create(@Valid @RequestBody ProductCategoryCreateDTO dto) {
        ProductCategory category = new ProductCategory();
        category.setCategoryCode(dto.getCategoryCode());
        category.setCategoryName(dto.getCategoryName());
        category.setParentId(dto.getParentId());
        category.setLevel(dto.getLevel());
        category.setSortOrder(dto.getSortOrder());
        category.setCategoryGroup(dto.getCategoryGroup());
        category.setDefaultUnit(dto.getDefaultUnit());
        return R.ok(productCategoryService.createCategory(category));
    }

    @GetMapping("/{id}")
    public R<ProductCategory> get(@PathVariable Long id) {
        return R.ok(productCategoryService.getById(id));
    }

    @GetMapping
    public R<List<ProductCategory>> list() {
        return R.ok(productCategoryService.list());
    }
}
