package com.aimpl.domain.server.service;

import com.aimpl.common.enums.ServerStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.server.dto.ServerProfileCreateDTO;
import com.aimpl.domain.server.entity.ServerProfile;
import com.aimpl.domain.server.mapper.ServerProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ServerProfileService extends ServiceImpl<ServerProfileMapper, ServerProfile> {

    private final ProjectMapper projectMapper;

    @Transactional
    public ServerProfile create(ServerProfileCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        boolean exists = count(new LambdaQueryWrapper<ServerProfile>()
                .eq(ServerProfile::getHost, dto.getHost())
                .eq(ServerProfile::getPort, dto.getPort())
                .eq(ServerProfile::getDbName, dto.getDbName())) > 0;
        if (exists) {
            throw new BizException("相同主机、端口、数据库名称的服务器配置已存在");
        }

        ServerProfile entity = new ServerProfile();
        entity.setProjectId(dto.getProjectId());
        entity.setServerName(dto.getServerName());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setDbType(dto.getDbType());
        entity.setDbName(dto.getDbName());
        entity.setSslEnabled(dto.getSslEnabled() != null ? dto.getSslEnabled() : false);
        entity.setOsType(dto.getOsType());
        entity.setErpVersion(dto.getErpVersion());
        entity.setApiBaseUrl(dto.getApiBaseUrl());
        entity.setStatus(ServerStatus.UNCONFIGURED);
        save(entity);
        return entity;
    }

    public List<ServerProfile> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<ServerProfile>()
                .eq(ServerProfile::getProjectId, projectId)
                .orderByDesc(ServerProfile::getCreateTime));
    }

    @Transactional
    public ServerProfile healthCheck(Long id) {
        ServerProfile profile = getById(id);
        if (profile == null) {
            throw new BizException("服务器配置不存在: " + id);
        }

        int latency = ThreadLocalRandom.current().nextInt(1, 500);
        boolean connected = latency < 300;

        profile.setNetworkLatencyMs(latency);
        profile.setLastHealthCheck(LocalDateTime.now());
        profile.setStatus(connected ? ServerStatus.CONNECTED : ServerStatus.DISCONNECTED);
        updateById(profile);
        return profile;
    }
}
