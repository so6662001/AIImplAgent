package com.aimpl.domain.aftersales.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.aftersales.entity.CustomerHealthRecord;
import com.aimpl.domain.aftersales.service.CustomerHealthService;
import com.aimpl.domain.aftersales.vo.CustomerHealthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-health")
@RequiredArgsConstructor
public class CustomerHealthController {

    private final CustomerHealthService customerHealthService;

    @PostMapping("/check/{projectId}")
    public R<CustomerHealthVO> check(@PathVariable Long projectId) {
        return R.ok(customerHealthService.checkHealth(projectId));
    }

    @GetMapping
    public R<CustomerHealthVO> getLatest(@RequestParam Long projectId) {
        CustomerHealthRecord record = customerHealthService.getLatest(projectId);
        if (record == null) {
            return R.ok(null);
        }
        return R.ok(customerHealthService.toVO(record));
    }

    @GetMapping("/history/{projectId}")
    public R<List<CustomerHealthRecord>> history(@PathVariable Long projectId) {
        return R.ok(customerHealthService.getHistory(projectId));
    }
}
