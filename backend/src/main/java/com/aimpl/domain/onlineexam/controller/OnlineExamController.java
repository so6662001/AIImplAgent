package com.aimpl.domain.onlineexam.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.onlineexam.dto.ExamPaperCreateDTO;
import com.aimpl.domain.onlineexam.entity.ExamPaper;
import com.aimpl.domain.onlineexam.entity.ExamSubmission;
import com.aimpl.domain.onlineexam.service.OnlineExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/online-exams")
@RequiredArgsConstructor
public class OnlineExamController {

    private final OnlineExamService onlineExamService;

    @PostMapping("/papers")
    public R<ExamPaper> createPaper(@Valid @RequestBody ExamPaperCreateDTO dto) {
        return R.ok(onlineExamService.createPaper(dto));
    }

    @GetMapping("/papers")
    public R<List<ExamPaper>> listPapers(@RequestParam(required = false) Long projectId) {
        return R.ok(onlineExamService.listPapers(projectId, null));
    }

    @GetMapping("/submissions")
    public R<List<ExamSubmission>> listSubmissions(@RequestParam(required = false) Long projectId) {
        return R.ok(onlineExamService.listSubmissions(projectId));
    }
}
