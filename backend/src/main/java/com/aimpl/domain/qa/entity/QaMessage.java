package com.aimpl.domain.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_qa_message")
public class QaMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String role;

    private String content;

    private String relatedModule;

    private String relatedVideoUrl;

    private Boolean helpful;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
