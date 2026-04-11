package com.aimpl.domain.knowledge.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.knowledge.dto.KnowledgeEntryCreateDTO;
import com.aimpl.domain.knowledge.entity.KnowledgeEntry;
import com.aimpl.domain.knowledge.service.KnowledgeService;
import com.aimpl.domain.knowledge.vo.KnowledgeStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public R<KnowledgeEntry> create(@Valid @RequestBody KnowledgeEntryCreateDTO dto) {
        return R.ok(knowledgeService.create(dto));
    }

    @GetMapping
    public R<List<KnowledgeEntry>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String layer,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String query) {
        return R.ok(knowledgeService.listEntries(category, layer, projectId, query));
    }

    @GetMapping("/{id}")
    public R<KnowledgeEntry> getById(@PathVariable Long id) {
        knowledgeService.incrementViewCount(id);
        return R.ok(knowledgeService.getById(id));
    }

    @PutMapping("/{id}")
    public R<KnowledgeEntry> update(@PathVariable Long id, @RequestBody KnowledgeEntryCreateDTO dto) {
        return R.ok(knowledgeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        knowledgeService.removeById(id);
        return R.ok();
    }

    @PostMapping("/{id}/helpful")
    public R<Void> markHelpful(@PathVariable Long id) {
        knowledgeService.markHelpful(id);
        return R.ok();
    }

    @GetMapping("/search")
    public R<List<KnowledgeEntry>> search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long projectId) {
        return R.ok(knowledgeService.search(q, category, projectId));
    }

    @GetMapping("/stats")
    public R<KnowledgeStatsVO> stats() {
        return R.ok(knowledgeService.getStats());
    }

    @PostMapping("/import-from-ticket/{ticketId}")
    public R<KnowledgeEntry> importFromTicket(@PathVariable Long ticketId) {
        return R.ok(knowledgeService.createFromResolution(ticketId));
    }
}
