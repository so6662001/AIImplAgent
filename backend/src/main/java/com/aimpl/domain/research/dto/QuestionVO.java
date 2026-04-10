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
public class QuestionVO {

    private String questionId;

    private String questionText;

    private String questionType;

    private boolean required;

    private List<String> options;

    private String hint;
}
