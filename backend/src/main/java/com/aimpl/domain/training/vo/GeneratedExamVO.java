package com.aimpl.domain.training.vo;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedExamVO {
    private String module;
    private String difficulty;
    private int totalQuestions;
    private int totalScore;
    private List<ExamQuestionVO> questions;
}
