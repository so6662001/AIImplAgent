package com.aimpl.domain.workforce.service;

import com.aimpl.common.enums.EvalRating;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.dto.ProjectEvaluationCreateDTO;
import com.aimpl.domain.workforce.entity.ProjectEvaluation;
import com.aimpl.domain.workforce.mapper.ProjectEvaluationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectEvaluationService extends ServiceImpl<ProjectEvaluationMapper, ProjectEvaluation> {

    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectEvaluation create(ProjectEvaluationCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        BigDecimal pqi = BigDecimal.valueOf(dto.getScheduleScore()).multiply(new BigDecimal("0.20"))
                .add(BigDecimal.valueOf(dto.getQualityScore()).multiply(new BigDecimal("0.30")))
                .add(BigDecimal.valueOf(dto.getCsatScore()).multiply(new BigDecimal("0.25")))
                .add(BigDecimal.valueOf(dto.getProcessScore()).multiply(new BigDecimal("0.15")))
                .add(BigDecimal.valueOf(dto.getCostScore()).multiply(new BigDecimal("0.10")))
                .setScale(2, RoundingMode.HALF_UP);

        EvalRating rating = determineRating(pqi);

        ProjectEvaluation entity = new ProjectEvaluation();
        entity.setProjectId(dto.getProjectId());
        entity.setEvaluatorId(dto.getEvaluatorId());
        entity.setScheduleScore(dto.getScheduleScore());
        entity.setQualityScore(dto.getQualityScore());
        entity.setCsatScore(dto.getCsatScore());
        entity.setProcessScore(dto.getProcessScore());
        entity.setCostScore(dto.getCostScore());
        entity.setPqiScore(pqi);
        entity.setRating(rating);
        entity.setAiComment(dto.getAiComment());
        save(entity);
        return entity;
    }

    public List<ProjectEvaluation> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<ProjectEvaluation>()
                .eq(ProjectEvaluation::getProjectId, projectId)
                .orderByDesc(ProjectEvaluation::getCreateTime));
    }

    private EvalRating determineRating(BigDecimal pqi) {
        double score = pqi.doubleValue();
        if (score >= 90) return EvalRating.EXCELLENT;
        if (score >= 80) return EvalRating.GOOD;
        if (score >= 60) return EvalRating.QUALIFIED;
        if (score >= 40) return EvalRating.NEEDS_IMPROVEMENT;
        return EvalRating.UNQUALIFIED;
    }
}
