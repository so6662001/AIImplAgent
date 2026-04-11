package com.aimpl.domain.simulation.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.simulation.service.MockDataGeneratorService;
import com.aimpl.domain.simulation.service.SceneScriptGeneratorService;
import com.aimpl.domain.simulation.service.SimulationReportService;
import com.aimpl.domain.simulation.vo.GeneratedSceneVO;
import com.aimpl.domain.simulation.vo.MockDataSetVO;
import com.aimpl.domain.simulation.vo.SimulationReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
public class SimulationAgentController {

    private final MockDataGeneratorService mockDataGeneratorService;
    private final SceneScriptGeneratorService sceneScriptGeneratorService;
    private final SimulationReportService simulationReportService;

    @PostMapping("/mock-data/generate/{projectId}")
    public R<MockDataSetVO> generateMockData(@PathVariable Long projectId) {
        return R.ok(mockDataGeneratorService.generateMockData(projectId));
    }

    @PostMapping("/scenes/auto-generate/{projectId}")
    public R<List<GeneratedSceneVO>> autoGenerateScenes(@PathVariable Long projectId) {
        return R.ok(sceneScriptGeneratorService.generateScenes(projectId));
    }

    @GetMapping("/reports/{projectId}")
    public R<SimulationReportVO> getEvaluationReport(@PathVariable Long projectId) {
        return R.ok(simulationReportService.generateEvaluationReport(projectId));
    }
}
