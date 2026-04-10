package com.aimpl.domain.agent.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.agent.dto.LlmProviderCreateDTO;
import com.aimpl.domain.agent.entity.LlmProviderConfig;
import com.aimpl.domain.agent.service.LlmProviderConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/llm-providers")
@RequiredArgsConstructor
public class LlmProviderConfigController {

    private final LlmProviderConfigService llmProviderConfigService;

    @PostMapping
    public R<LlmProviderConfig> create(@Valid @RequestBody LlmProviderCreateDTO dto) {
        return R.ok(llmProviderConfigService.createProvider(dto));
    }

    @GetMapping
    public R<List<LlmProviderConfig>> list() {
        return R.ok(llmProviderConfigService.listMasked());
    }

    @GetMapping("/{id}")
    public R<LlmProviderConfig> get(@PathVariable Long id) {
        return R.ok(llmProviderConfigService.getByIdMasked(id));
    }

    @PutMapping("/{id}")
    public R<LlmProviderConfig> update(@PathVariable Long id, @Valid @RequestBody LlmProviderCreateDTO dto) {
        return R.ok(llmProviderConfigService.updateProvider(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        llmProviderConfigService.removeById(id);
        return R.ok();
    }
}
