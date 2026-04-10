package com.aimpl.domain.workforce.entity;

import com.aimpl.common.enums.EvalRating;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_project_evaluation")
public class ProjectEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long evaluatorId;

    private Integer scheduleScore;

    private Integer qualityScore;

    private Integer csatScore;

    private Integer processScore;

    private Integer costScore;

    private BigDecimal pqiScore;

    private EvalRating rating;

    private String aiComment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
