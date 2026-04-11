package com.aimpl.domain.dataimport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_import_progress")
public class ImportProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Integer currentBatch;

    private String batch1Status;

    private String batch2Status;

    private String batch3Status;

    private String batch4Status;

    private String batch5Status;

    private String overallStatus;

    private String lastError;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
