package com.aimpl.domain.assist.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.assist.dto.SupportTicketCreateDTO;
import com.aimpl.domain.assist.entity.SupportTicket;
import com.aimpl.domain.assist.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public R<SupportTicket> create(@Valid @RequestBody SupportTicketCreateDTO dto) {
        return R.ok(supportTicketService.createTicket(dto));
    }

    @GetMapping
    public R<List<SupportTicket>> list(@RequestParam Long projectId) {
        return R.ok(supportTicketService.listByProject(projectId));
    }

    @PutMapping("/{id}/resolve")
    public R<SupportTicket> resolve(@PathVariable Long id, @RequestParam String resolution) {
        return R.ok(supportTicketService.resolveTicket(id, resolution));
    }

    @GetMapping("/stats/{projectId}")
    public R<Map<String, Object>> stats(@PathVariable Long projectId) {
        return R.ok(supportTicketService.getTicketStats(projectId));
    }
}
