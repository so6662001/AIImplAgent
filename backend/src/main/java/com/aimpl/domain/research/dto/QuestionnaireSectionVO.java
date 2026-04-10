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
public class QuestionnaireSectionVO {

    private String sectionName;

    private String sectionDescription;

    private List<QuestionVO> questions;
}
