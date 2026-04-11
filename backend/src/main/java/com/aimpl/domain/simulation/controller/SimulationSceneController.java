package com.aimpl.domain.simulation.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.simulation.dto.SimulationExecuteDTO;
import com.aimpl.domain.simulation.dto.SimulationSceneCreateDTO;
import com.aimpl.domain.simulation.entity.SimulationScene;
import com.aimpl.domain.simulation.service.SimulationSceneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulation-scenes")
@RequiredArgsConstructor
public class SimulationSceneController {

    private final SimulationSceneService simulationSceneService;

    @PostMapping
    public R<SimulationScene> create(@Valid @RequestBody SimulationSceneCreateDTO dto) {
        return R.ok(simulationSceneService.create(dto));
    }

    @GetMapping
    public R<List<SimulationScene>> list(@RequestParam Long projectId) {
        return R.ok(simulationSceneService.listByProjectId(projectId));
    }

    @PutMapping("/{id}/execute")
    public R<SimulationScene> execute(@PathVariable Long id, @Valid @RequestBody SimulationExecuteDTO dto) {
        return R.ok(simulationSceneService.execute(id, dto));
    }
}
