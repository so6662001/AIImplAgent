package com.aimpl.domain.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_submission")
public class ExamSubmission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long clientUserId;

    private String answers;

    private Integer score;

    private Boolean passed;

    private Integer correctCount;

    private Integer wrongCount;

    private String wrongQuestions;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
