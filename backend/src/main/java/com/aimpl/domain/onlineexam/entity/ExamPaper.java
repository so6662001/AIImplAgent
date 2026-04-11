package com.aimpl.domain.onlineexam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_paper")
public class ExamPaper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String module;

    private String difficulty;

    private String title;

    private Integer totalQuestions;

    private Integer totalScore;

    private String questions;

    private Long createdFor;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
