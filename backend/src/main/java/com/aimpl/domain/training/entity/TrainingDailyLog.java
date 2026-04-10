package com.aimpl.domain.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_training_daily_log")
public class TrainingDailyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private LocalDate logDate;

    private String topic;

    private String trainerName;

    private Integer attendeeCount;

    private Boolean signInCompleted;

    private Boolean coursewareUploaded;

    private Boolean summaryUploaded;

    private Boolean examConducted;

    private Boolean dailyReportSubmitted;

    private String issues;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
