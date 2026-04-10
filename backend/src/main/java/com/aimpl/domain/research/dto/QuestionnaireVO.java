package com.aimpl.domain.research.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireVO {

    private String title;

    private String industryType;

    private List<QuestionnaireSectionVO> sections;
}
