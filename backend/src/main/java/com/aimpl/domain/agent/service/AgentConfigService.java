package com.aimpl.domain.agent.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.agent.dto.AgentConfigCreateDTO;
import com.aimpl.domain.agent.entity.AgentConfig;
import com.aimpl.domain.agent.mapper.AgentConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentConfigService extends ServiceImpl<AgentConfigMapper, AgentConfig> {

    private final LlmProviderConfigService llmProviderConfigService;

    public AgentConfig createAgent(AgentConfigCreateDTO dto) {
        boolean exists = count(new LambdaQueryWrapper<AgentConfig>()
                .eq(AgentConfig::getAgentCode, dto.getAgentCode())) > 0;
        if (exists) {
            throw new BizException("智能体编码已存在: " + dto.getAgentCode());
        }

        validateLlmProvider(dto.getLlmProviderId());
        if (dto.getFallbackLlmProviderId() != null) {
            validateLlmProvider(dto.getFallbackLlmProviderId());
        }

        AgentConfig config = new AgentConfig();
        config.setAgentCode(dto.getAgentCode());
        config.setAgentName(dto.getAgentName());
        config.setDescription(dto.getDescription());
        config.setLlmProviderId(dto.getLlmProviderId());
        config.setFallbackLlmProviderId(dto.getFallbackLlmProviderId());
        config.setPromptTemplate(dto.getPromptTemplate());
        config.setRagEnabled(dto.getRagEnabled() != null ? dto.getRagEnabled() : false);
        config.setRagCollectionName(dto.getRagCollectionName());
        config.setEnabled(true);
        save(config);
        return config;
    }

    public AgentConfig updateAgent(Long id, AgentConfigCreateDTO dto) {
        AgentConfig config = getById(id);
        if (config == null) {
            throw new BizException("智能体配置不存在");
        }

        AgentConfig existing = getOne(new LambdaQueryWrapper<AgentConfig>()
                .eq(AgentConfig::getAgentCode, dto.getAgentCode())
                .ne(AgentConfig::getId, id));
        if (existing != null) {
            throw new BizException("智能体编码已存在: " + dto.getAgentCode());
        }

        validateLlmProvider(dto.getLlmProviderId());
        if (dto.getFallbackLlmProviderId() != null) {
            validateLlmProvider(dto.getFallbackLlmProviderId());
        }

        config.setAgentCode(dto.getAgentCode());
        config.setAgentName(dto.getAgentName());
        config.setDescription(dto.getDescription());
        config.setLlmProviderId(dto.getLlmProviderId());
        config.setFallbackLlmProviderId(dto.getFallbackLlmProviderId());
        config.setPromptTemplate(dto.getPromptTemplate());
        config.setRagEnabled(dto.getRagEnabled() != null ? dto.getRagEnabled() : false);
        config.setRagCollectionName(dto.getRagCollectionName());
        updateById(config);
        return config;
    }

    private void validateLlmProvider(Long providerId) {
        if (llmProviderConfigService.getById(providerId) == null) {
            throw new BizException("LLM供应商不存在, id: " + providerId);
        }
    }
}
