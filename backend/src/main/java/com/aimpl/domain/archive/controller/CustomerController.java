package com.aimpl.domain.archive.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.archive.dto.CustomerCreateDTO;
import com.aimpl.domain.archive.entity.Customer;
import com.aimpl.domain.archive.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public R<Customer> create(@Valid @RequestBody CustomerCreateDTO dto) {
        return R.ok(customerService.createCustomer(dto));
    }

    @GetMapping("/{id}")
    public R<Customer> get(@PathVariable Long id) {
        return R.ok(customerService.getById(id));
    }

    @GetMapping
    public R<List<Customer>> list() {
        return R.ok(customerService.list());
    }
}
