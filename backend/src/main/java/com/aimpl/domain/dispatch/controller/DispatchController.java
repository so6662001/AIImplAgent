package com.aimpl.domain.dispatch.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.dispatch.dto.DispatchAssignDTO;
import com.aimpl.domain.dispatch.dto.DispatchRequestDTO;
import com.aimpl.domain.dispatch.service.DispatchService;
import com.aimpl.domain.dispatch.vo.DispatchResultVO;
import com.aimpl.domain.project.entity.Project;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping("/recommend")
    public R<DispatchResultVO> recommend(@Valid @RequestBody DispatchRequestDTO request) {
        return R.ok(dispatchService.recommend(request));
    }

    @PostMapping("/assign")
    public R<Project> assign(@Valid @RequestBody DispatchAssignDTO dto) {
        return R.ok(dispatchService.assign(dto));
    }
}
