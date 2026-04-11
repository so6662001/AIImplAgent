package com.aimpl.domain.simulation.service;

import com.aimpl.common.enums.SceneType;
import com.aimpl.common.enums.SimulationStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.simulation.dto.SimulationExecuteDTO;
import com.aimpl.domain.simulation.dto.SimulationSceneCreateDTO;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.mapper.SimulationSceneMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationSceneService extends ServiceImpl<SimulationSceneMapper, SimulationScene> {

    private final ProjectMapper projectMapper;

    @Transactional
    public SimulationScene create(SimulationSceneCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        boolean nameExists = count(new LambdaQueryWrapper<SimulationScene>()
                .eq(SimulationScene::getProjectId, dto.getProjectId())
                .eq(SimulationScene::getSceneName, dto.getSceneName())) > 0;
        if (nameExists) {
            throw new BizException("该项目下场景名称已存在: " + dto.getSceneName());
        }

        SceneType sceneType;
        try {
            sceneType = SceneType.valueOf(dto.getSceneType());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的场景类型: " + dto.getSceneType());
        }

        SimulationScene entity = new SimulationScene();
        entity.setProjectId(dto.getProjectId());
        entity.setSceneName(dto.getSceneName());
        entity.setSceneType(sceneType);
        entity.setDescription(dto.getDescription());
        entity.setSteps(dto.getSteps());
        entity.setExpectedResult(dto.getExpectedResult());
        entity.setStatus(SimulationStatus.PENDING);
        save(entity);
        return entity;
    }

    public List<SimulationScene> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<SimulationScene>()
                .eq(SimulationScene::getProjectId, projectId)
                .orderByDesc(SimulationScene::getCreateTime));
    }

    @Transactional
    public SimulationScene execute(Long id, SimulationExecuteDTO dto) {
        SimulationScene scene = getById(id);
        if (scene == null) {
            throw new BizException("模拟场景不存在: " + id);
        }

        SimulationStatus status;
        try {
            status = SimulationStatus.valueOf(dto.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的执行状态: " + dto.getStatus());
        }

        scene.setActualResult(dto.getActualResult());
        scene.setStatus(status);
        scene.setExecutedAt(LocalDateTime.now());
        scene.setExecutedBy(dto.getExecutedBy());
        scene.setDeviation(dto.getDeviation());
        updateById(scene);
        return scene;
    }
}
