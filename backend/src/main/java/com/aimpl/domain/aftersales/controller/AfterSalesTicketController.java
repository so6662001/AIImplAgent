package com.aimpl.domain.aftersales.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.aftersales.dto.AfterSalesTicketCreateDTO;
import com.aimpl.domain.aftersales.entity.AfterSalesTicket;
import com.aimpl.domain.aftersales.service.AfterSalesTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/after-sales-tickets")
@RequiredArgsConstructor
public class AfterSalesTicketController {

    private final AfterSalesTicketService afterSalesTicketService;

    @PostMapping
    public R<AfterSalesTicket> create(@Valid @RequestBody AfterSalesTicketCreateDTO dto) {
        return R.ok(afterSalesTicketService.createTicket(dto));
    }

    @GetMapping
    public R<List<AfterSalesTicket>> list(@RequestParam Long projectId) {
        return R.ok(afterSalesTicketService.listByProject(projectId));
    }

    @PutMapping("/{id}/resolve")
    public R<AfterSalesTicket> resolve(@PathVariable Long id,
                                       @RequestParam String resolution,
                                       @RequestParam(defaultValue = "false") boolean knowledgeCreated) {
        return R.ok(afterSalesTicketService.resolveTicket(id, resolution, knowledgeCreated));
    }

    @PutMapping("/{id}/rate")
    public R<Void> rate(@PathVariable Long id, @RequestParam int satisfaction) {
        afterSalesTicketService.rateTicket(id, satisfaction);
        return R.ok();
    }

    @GetMapping("/stats/{projectId}")
    public R<Map<String, Object>> stats(@PathVariable Long projectId) {
        return R.ok(afterSalesTicketService.getStats(projectId));
    }
}
