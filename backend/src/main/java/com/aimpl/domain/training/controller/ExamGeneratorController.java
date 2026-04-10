package com.aimpl.domain.training.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.training.dto.ExamGenerateRequestDTO;
import com.aimpl.domain.training.service.ExamQuestionGeneratorService;
import com.aimpl.domain.training.service.ExamWeakPointAnalysisService;
import com.aimpl.domain.training.vo.GeneratedExamVO;
import com.aimpl.domain.training.vo.WeakPointAnalysisVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class ExamGeneratorController {

    private final ExamQuestionGeneratorService examQuestionGeneratorService;
    private final ExamWeakPointAnalysisService examWeakPointAnalysisService;

    @PostMapping("/exam-generator/generate")
    public R<GeneratedExamVO> generateExam(@Valid @RequestBody ExamGenerateRequestDTO dto) {
        return R.ok(examQuestionGeneratorService.generateExam(
                dto.getProjectId(), dto.getModule(), dto.getDifficulty()));
    }

    @GetMapping("/weak-points/{projectId}/{traineeId}")
    public R<WeakPointAnalysisVO> analyzeWeakPoints(
            @PathVariable Long projectId,
            @PathVariable Long traineeId) {
        return R.ok(examWeakPointAnalysisService.analyzeWeakPoints(projectId, traineeId));
    }
}
