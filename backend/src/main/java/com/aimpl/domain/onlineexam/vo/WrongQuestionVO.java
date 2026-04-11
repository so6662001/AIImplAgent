package com.aimpl.domain.onlineexam.vo;

import lombok.Data;

@Data
public class WrongQuestionVO {
    private int questionId;
    private String correctAnswer;
    private String userAnswer;
    private String questionText;
}
