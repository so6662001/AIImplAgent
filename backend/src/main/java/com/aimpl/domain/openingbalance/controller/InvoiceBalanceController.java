package com.aimpl.domain.openingbalance.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.openingbalance.dto.InvoiceBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.InvoiceBalance;
import com.aimpl.domain.openingbalance.service.InvoiceBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-balances")
@RequiredArgsConstructor
public class InvoiceBalanceController {

    private final InvoiceBalanceService invoiceBalanceService;

    @PostMapping
    public R<InvoiceBalance> create(@Valid @RequestBody InvoiceBalanceCreateDTO dto) {
        return R.ok(invoiceBalanceService.create(dto));
    }

    @GetMapping
    public R<List<InvoiceBalance>> list(@RequestParam Long projectId,
                                        @RequestParam(required = false) String invoiceType) {
        return R.ok(invoiceBalanceService.listByProjectAndType(projectId, invoiceType));
    }
}
