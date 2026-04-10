package com.aimpl.domain.qa.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.qa.dto.QaAskDTO;
import com.aimpl.domain.qa.dto.QaMessageVO;
import com.aimpl.domain.qa.dto.QaSessionVO;
import com.aimpl.domain.qa.service.QaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    @PostMapping("/ask")
    public R<QaMessageVO> ask(@Valid @RequestBody QaAskDTO dto, HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(qaService.ask(clientUserId, dto));
    }

    @GetMapping("/sessions")
    public R<List<QaSessionVO>> listSessions(HttpServletRequest request) {
        Long clientUserId = (Long) request.getAttribute("clientUserId");
        return R.ok(qaService.getUserSessions(clientUserId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public R<List<QaMessageVO>> listMessages(@PathVariable Long sessionId) {
        return R.ok(qaService.getSessionMessages(sessionId));
    }

    @PostMapping("/messages/{messageId}/rate")
    public R<Void> rateMessage(@PathVariable Long messageId, @RequestParam Boolean helpful) {
        qaService.rateMessage(messageId, helpful);
        return R.ok();
    }
}
