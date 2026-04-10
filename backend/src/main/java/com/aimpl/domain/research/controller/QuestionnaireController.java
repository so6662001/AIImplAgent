package com.aimpl.domain.research.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.research.dto.QuestionnaireRequestDTO;
import com.aimpl.domain.research.dto.QuestionnaireVO;
import com.aimpl.domain.research.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/research/questionnaire")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    @PostMapping("/generate")
    public R<QuestionnaireVO> generate(@Valid @RequestBody QuestionnaireRequestDTO dto) {
        QuestionnaireVO vo = questionnaireService.generate(
                dto.getIndustryType(), dto.getScale(), dto.getModules());
        return R.ok(vo);
    }
}
