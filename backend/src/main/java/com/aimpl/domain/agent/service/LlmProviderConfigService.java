package com.aimpl.domain.agent.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.agent.dto.LlmProviderCreateDTO;
import com.aimpl.domain.agent.entity.LlmProviderConfig;
import com.aimpl.domain.agent.mapper.LlmProviderConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LlmProviderConfigService extends ServiceImpl<LlmProviderConfigMapper, LlmProviderConfig> {

    public LlmProviderConfig createProvider(LlmProviderCreateDTO dto) {
        boolean exists = count(new LambdaQueryWrapper<LlmProviderConfig>()
                .eq(LlmProviderConfig::getProviderName, dto.getProviderName())) > 0;
        if (exists) {
            throw new BizException("供应商名称已存在: " + dto.getProviderName());
        }

        LlmProviderConfig config = new LlmProviderConfig();
        config.setProviderName(dto.getProviderName());
        config.setProviderType(dto.getProviderType());
        config.setApiEndpoint(dto.getApiEndpoint());
        config.setApiKey(dto.getApiKey());
        config.setModelName(dto.getModelName());
        config.setMaxTokens(dto.getMaxTokens());
        config.setTemperature(dto.getTemperature());
        config.setEnabled(true);
        try {
            save(config);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("供应商名称已存在: " + dto.getProviderName());
        }
        return maskApiKey(config);
    }

    public LlmProviderConfig updateProvider(Long id, LlmProviderCreateDTO dto) {
        LlmProviderConfig config = getById(id);
        if (config == null) {
            throw new BizException("供应商配置不存在");
        }

        LlmProviderConfig existing = getOne(new LambdaQueryWrapper<LlmProviderConfig>()
                .eq(LlmProviderConfig::getProviderName, dto.getProviderName())
                .ne(LlmProviderConfig::getId, id));
        if (existing != null) {
            throw new BizException("供应商名称已存在: " + dto.getProviderName());
        }

        config.setProviderName(dto.getProviderName());
        config.setProviderType(dto.getProviderType());
        config.setApiEndpoint(dto.getApiEndpoint());
        config.setApiKey(dto.getApiKey());
        config.setModelName(dto.getModelName());
        config.setMaxTokens(dto.getMaxTokens());
        config.setTemperature(dto.getTemperature());
        updateById(config);
        return maskApiKey(config);
    }

    public LlmProviderConfig getByIdMasked(Long id) {
        LlmProviderConfig config = getById(id);
        if (config == null) {
            throw new BizException("供应商配置不存在");
        }
        return maskApiKey(config);
    }

    public List<LlmProviderConfig> listMasked() {
        return list().stream()
                .map(this::maskApiKey)
                .collect(Collectors.toList());
    }

    private LlmProviderConfig maskApiKey(LlmProviderConfig config) {
        if (config.getApiKey() != null) {
            if (config.getApiKey().length() <= 4) {
                config.setApiKey("****");
            } else {
                String masked = "****" + config.getApiKey().substring(config.getApiKey().length() - 4);
                config.setApiKey(masked);
            }
        }
        return config;
    }
}
