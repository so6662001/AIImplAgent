package com.aimpl.domain.workforce.service;

import com.aimpl.common.enums.EngineerLevel;
import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.entity.ProjectEvaluation;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.mapper.ProjectEvaluationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EngineerService extends ServiceImpl<EngineerMapper, Engineer> {

    private final ProjectMapper projectMapper;
    private final ProjectEvaluationMapper projectEvaluationMapper;

    @Transactional
    public Engineer create(EngineerCreateDTO dto) {
        boolean codeExists = count(new LambdaQueryWrapper<Engineer>()
                .eq(Engineer::getEngineerCode, dto.getEngineerCode())) > 0;
        if (codeExists) {
            throw new BizException("工程师编号已存在: " + dto.getEngineerCode());
        }

        EngineerLevel level;
        try {
            level = EngineerLevel.valueOf(dto.getLevel());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的工程师级别: " + dto.getLevel());
        }

        Engineer entity = new Engineer();
        entity.setEngineerCode(dto.getEngineerCode());
        entity.setName(dto.getName());
        entity.setLevel(level);
        entity.setSkills(dto.getSkills());
        entity.setCurrentStatus(EngineerStatus.IDLE);
        entity.setJoinDate(dto.getJoinDate());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setMonthlyProjectCount(0);
        save(entity);
        return entity;
    }

    @Transactional
    public void updateCompositeScoreFromEvaluations(Long engineerId) {
        Engineer engineer = getById(engineerId);
        if (engineer == null) {
            return;
        }

        List<Long> projectIds = projectMapper.selectList(
                new LambdaQueryWrapper<Project>().eq(Project::getPmId, engineerId)
        ).stream().map(Project::getId).collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return;
        }

        List<ProjectEvaluation> evaluations = projectEvaluationMapper.selectList(
                new LambdaQueryWrapper<ProjectEvaluation>()
                        .in(ProjectEvaluation::getProjectId, projectIds)
                        .orderByAsc(ProjectEvaluation::getCreateTime)
        );

        if (evaluations.isEmpty()) {
            return;
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightTotal = BigDecimal.ZERO;
        for (int i = 0; i < evaluations.size(); i++) {
            BigDecimal weight = BigDecimal.valueOf(i + 1);
            BigDecimal pqi = evaluations.get(i).getPqiScore();
            if (pqi != null) {
                weightedSum = weightedSum.add(pqi.multiply(weight));
                weightTotal = weightTotal.add(weight);
            }
        }

        if (weightTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal compositeScore = weightedSum.divide(weightTotal, 2, RoundingMode.HALF_UP);
            engineer.setCompositeScore(compositeScore);
            updateById(engineer);
        }
    }
}
