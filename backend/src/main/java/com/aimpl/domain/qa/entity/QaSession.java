package com.aimpl.domain.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_qa_session")
public class QaSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long clientUserId;

    private String title;

    private String status;

    private Integer messageCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
