package com.aimpl.domain.onlineexam.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.onlineexam.dto.ExamAnswerSubmitDTO;
import com.aimpl.domain.onlineexam.entity.ExamPaper;
import com.aimpl.domain.onlineexam.entity.ExamSubmission;
import com.aimpl.domain.onlineexam.service.OnlineExamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/exams")
@RequiredArgsConstructor
public class ClientExamController {

    private final OnlineExamService onlineExamService;

    @GetMapping("/my-papers")
    public R<List<ExamPaper>> listMyPapers(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        Long projectId = (Long) request.getAttribute("projectId");
        return R.ok(onlineExamService.listPapers(projectId, clientUserId));
    }

    @GetMapping("/papers/{paperId}")
    public R<ExamPaper> getPaper(@PathVariable Long paperId) {
        ExamPaper paper = onlineExamService.getById(paperId);
        if (paper == null) {
            return R.fail("试卷不存在");
        }
        return R.ok(paper);
    }

    @PostMapping("/submit")
    public R<ExamSubmission> submitAnswers(@Valid @RequestBody ExamAnswerSubmitDTO dto,
                                            HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        dto.setClientUserId(clientUserId);
        return R.ok(onlineExamService.submitAnswers(dto));
    }

    @GetMapping("/my-results")
    public R<List<ExamSubmission>> listMyResults(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(onlineExamService.listMySubmissions(clientUserId));
    }
}
