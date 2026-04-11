package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    public R<ExamRecord> submit(@Valid @RequestBody ExamRecordCreateDTO dto) {
        return R.ok(examService.submitExam(dto));
    }

    @GetMapping("/go-live-check/{projectId}")
    public R<Map<String, Object>> goLiveCheck(@PathVariable Long projectId) {
        boolean ready = examService.checkGoLiveReadiness(projectId);
        return R.ok(Map.of(
                "projectId", projectId,
                "goLiveReady", ready,
                "message", ready ? "所有KA用户必学课程已通过，允许推进上线"
                        : "存在KA用户必学课程未通过，不允许推进上线"
        ));
    }
}
