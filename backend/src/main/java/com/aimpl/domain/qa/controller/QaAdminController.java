package com.aimpl.domain.qa.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.qa.dto.QaMessageVO;
import com.aimpl.domain.qa.dto.QaReplyDTO;
import com.aimpl.domain.qa.dto.QaSessionVO;
import com.aimpl.domain.qa.service.QaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qa-sessions")
@RequiredArgsConstructor
public class QaAdminController {

    private final QaService qaService;

    @GetMapping
    public R<List<QaSessionVO>> listSessions(@RequestParam Long projectId) {
        return R.ok(qaService.getProjectSessions(projectId));
    }

    @GetMapping("/{sessionId}/messages")
    public R<List<QaMessageVO>> listMessages(@PathVariable Long sessionId) {
        return R.ok(qaService.getSessionMessages(sessionId, null));
    }

    @PostMapping("/{sessionId}/reply")
    public R<QaMessageVO> reply(@PathVariable Long sessionId,
                                @Valid @RequestBody QaReplyDTO dto) {
        return R.ok(qaService.adminReply(sessionId, dto));
    }
}
