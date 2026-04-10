package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.ProductCreateDTO;
import com.aimpl.domain.archive.entity.Product;
import com.aimpl.domain.archive.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public R<Product> create(@Valid @RequestBody ProductCreateDTO dto) {
        return R.ok(productService.createProduct(dto));
    }

    @GetMapping("/{id}")
    public R<Product> get(@PathVariable Long id) {
        return R.ok(productService.getById(id));
    }

    @GetMapping
    public R<List<Product>> list() {
        return R.ok(productService.list());
    }
}
