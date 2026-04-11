package com.aimpl.domain.simulation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SimulationReportVO {
    private Long projectId;
    private LocalDateTime generatedAt;
    private int totalScenes;
    private int executedScenes;
    private int passedScenes;
    private int failedScenes;
    private int pendingScenes;
    private BigDecimal overallPassRate;
    private BigDecimal overallScore;
    private boolean readyForTraining;
    private List<SceneResultVO> sceneResults;
    private List<String> recommendations;
}
