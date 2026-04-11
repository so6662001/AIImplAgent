package com.aimpl.domain.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_learning_progress")
public class LearningProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long clientUserId;

    private Long courseId;

    private Long chapterId;

    private String status;

    private Integer watchedDuration;

    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
