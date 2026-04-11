package com.aimpl.domain.research.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.research.dto.CustomerProfileCreateDTO;
import com.aimpl.domain.research.entity.CustomerProfile;
import com.aimpl.domain.research.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-profiles")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @PostMapping
    public R<CustomerProfile> create(@Valid @RequestBody CustomerProfileCreateDTO dto) {
        return R.ok(customerProfileService.create(dto));
    }

    @GetMapping("/{id}")
    public R<CustomerProfile> get(@PathVariable Long id) {
        return R.ok(customerProfileService.getById(id));
    }

    @GetMapping
    public R<List<CustomerProfile>> list(@RequestParam Long projectId) {
        return R.ok(customerProfileService.listByProjectId(projectId));
    }
}
