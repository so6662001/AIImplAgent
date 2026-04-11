package com.aimpl.domain.docgen.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.docgen.dto.DocumentGenerateRequestDTO;
import com.aimpl.domain.docgen.service.DocumentGenerationService;
import com.aimpl.domain.docgen.vo.DocumentTypeVO;
import com.aimpl.domain.docgen.vo.GeneratedDocumentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentGenerationService documentGenerationService;

    @PostMapping("/generate")
    public R<GeneratedDocumentVO> generate(@Valid @RequestBody DocumentGenerateRequestDTO request) {
        return R.ok(documentGenerationService.generate(
                request.getDocumentType(),
                request.getProjectId(),
                request.getParams()));
    }

    @GetMapping("/types/{projectId}")
    public R<List<DocumentTypeVO>> listDocumentTypes(@PathVariable Long projectId) {
        return R.ok(documentGenerationService.listDocumentTypes(projectId));
    }
}
