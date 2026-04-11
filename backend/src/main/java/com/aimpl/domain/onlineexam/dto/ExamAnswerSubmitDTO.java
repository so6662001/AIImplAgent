package com.aimpl.domain.onlineexam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ExamAnswerSubmitDTO {

    @NotNull(message = "试卷ID不能为空")
    private Long paperId;

    @NotNull(message = "用户ID不能为空")
    private Long clientUserId;

    @NotNull(message = "答案不能为空")
    private Map<Integer, String> answers;

    @Min(value = 0, message = "答题时长不能为负")
    private Integer duration;
}
