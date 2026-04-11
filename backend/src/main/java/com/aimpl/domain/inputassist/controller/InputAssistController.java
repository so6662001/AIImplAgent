package com.aimpl.domain.inputassist.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.inputassist.dto.InputAssistLogDTO;
import com.aimpl.domain.inputassist.entity.InputAssistLog;
import com.aimpl.domain.inputassist.service.InputAssistService;
import com.aimpl.domain.inputassist.vo.InputAssistStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/input-assist")
@RequiredArgsConstructor
public class InputAssistController {

    private final InputAssistService inputAssistService;

    @PostMapping("/logs")
    public R<InputAssistLog> createLog(@Valid @RequestBody InputAssistLogDTO dto) {
        return R.ok(inputAssistService.logAssist(dto));
    }

    @GetMapping("/logs/stats")
    public R<InputAssistStatsVO> getStats(@RequestParam String page) {
        return R.ok(inputAssistService.getStats(page));
    }

    @GetMapping("/logs")
    public R<List<InputAssistLog>> listLogs(@RequestParam String page) {
        return R.ok(inputAssistService.listByPage(page));
    }
}
