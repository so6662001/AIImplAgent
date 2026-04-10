package com.aimpl.domain.training.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExamQuestionVO {
    private int questionId;
    private String questionType;
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private int score;
    private String hint;
}
