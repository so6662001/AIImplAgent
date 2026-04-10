package com.aimpl.domain.simulation.service;

import com.aimpl.common.enums.SimulationStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.simulation.entity.SimulationReport;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.mapper.SimulationReportMapper;
import com.aimpl.domain.simulation.mapper.SimulationSceneMapper;
import com.aimpl.domain.simulation.vo.SceneResultVO;
import com.aimpl.domain.simulation.vo.SimulationReportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationReportService {

    private final ProjectMapper projectMapper;
    private final SimulationSceneMapper simulationSceneMapper;
    private final SimulationReportMapper simulationReportMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public SimulationReportVO generateEvaluationReport(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        List<SimulationScene> scenes = simulationSceneMapper.selectList(
                new LambdaQueryWrapper<SimulationScene>()
                        .eq(SimulationScene::getProjectId, projectId)
                        .orderByAsc(SimulationScene::getCreateTime));

        if (scenes.isEmpty()) {
            throw new BizException("该项目下没有模拟演练场景，请先生成场景");
        }

        int total = scenes.size();
        int passed = 0;
        int failed = 0;
        int pending = 0;
        BigDecimal scoreSum = BigDecimal.ZERO;
        int scoredCount = 0;

        List<SceneResultVO> sceneResults = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        for (SimulationScene scene : scenes) {
            SceneResultVO result = new SceneResultVO();
            result.setSceneName(scene.getSceneName());
            result.setSceneType(scene.getSceneType() != null ? scene.getSceneType().name() : "");
            result.setStatus(scene.getStatus() != null ? scene.getStatus().name() : "PENDING");
            result.setDeviation(scene.getDeviation());

            if (scene.getStatus() == SimulationStatus.PASSED) {
                passed++;
                if (scene.getDeviation() != null && !scene.getDeviation().isBlank()) {
                    result.setScore(new BigDecimal("80"));
                    result.setRecommendation("场景通过但存在偏差，建议复查：" + scene.getDeviation());
                } else {
                    result.setScore(new BigDecimal("100"));
                    result.setRecommendation("场景执行完全符合预期");
                }
                scoreSum = scoreSum.add(result.getScore());
                scoredCount++;
            } else if (scene.getStatus() == SimulationStatus.FAILED) {
                failed++;
                result.setScore(BigDecimal.ZERO);
                String failReason = scene.getActualResult() != null ? scene.getActualResult() : "未知原因";
                result.setRecommendation("场景执行失败，需重点排查：" + failReason);
                recommendations.add("【" + scene.getSceneName() + "】执行失败：" + failReason);
                scoreSum = scoreSum.add(BigDecimal.ZERO);
                scoredCount++;
            } else {
                pending++;
                result.setScore(null);
                result.setRecommendation("尚未执行，请安排演练");
            }

            sceneResults.add(result);
        }

        int executed = passed + failed;
        BigDecimal passRate = BigDecimal.ZERO;
        if (executed > 0) {
            passRate = new BigDecimal(passed)
                    .divide(new BigDecimal(executed), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal overallScore = BigDecimal.ZERO;
        if (scoredCount > 0) {
            overallScore = scoreSum.divide(new BigDecimal(scoredCount), 2, RoundingMode.HALF_UP);
        }

        boolean readyForTraining = passRate.compareTo(new BigDecimal("80")) >= 0;

        if (pending > 0) {
            recommendations.add("仍有" + pending + "个场景未执行，建议尽快安排演练");
        }
        if (!readyForTraining && executed > 0) {
            recommendations.add("整体通过率" + passRate + "%，未达到80%上线标准，建议对失败场景重新演练");
        }
        if (readyForTraining) {
            recommendations.add("模拟演练整体通过率达标，可以进入培训上线阶段");
        }
        if (failed > 0) {
            recommendations.add("共有" + failed + "个场景失败，建议重点关注对应业务模块的配置和数据准确性");
        }

        SimulationReportVO reportVO = new SimulationReportVO();
        reportVO.setProjectId(projectId);
        reportVO.setGeneratedAt(LocalDateTime.now());
        reportVO.setTotalScenes(total);
        reportVO.setExecutedScenes(executed);
        reportVO.setPassedScenes(passed);
        reportVO.setFailedScenes(failed);
        reportVO.setPendingScenes(pending);
        reportVO.setOverallPassRate(passRate);
        reportVO.setOverallScore(overallScore);
        reportVO.setReadyForTraining(readyForTraining);
        reportVO.setSceneResults(sceneResults);
        reportVO.setRecommendations(recommendations);

        persistReport(reportVO);

        return reportVO;
    }

    private void persistReport(SimulationReportVO reportVO) {
        SimulationReport entity = new SimulationReport();
        entity.setProjectId(reportVO.getProjectId());
        entity.setTotalScenes(reportVO.getTotalScenes());
        entity.setPassedScenes(reportVO.getPassedScenes());
        entity.setFailedScenes(reportVO.getFailedScenes());
        entity.setOverallPassRate(reportVO.getOverallPassRate());
        entity.setOverallScore(reportVO.getOverallScore());
        entity.setReadyForTraining(reportVO.isReadyForTraining());
        try {
            entity.setReportContent(objectMapper.writeValueAsString(reportVO));
        } catch (JsonProcessingException e) {
            entity.setReportContent("{}");
        }
        simulationReportMapper.insert(entity);
    }
}
