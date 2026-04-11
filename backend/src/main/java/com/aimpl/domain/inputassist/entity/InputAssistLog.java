package com.aimpl.domain.inputassist.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_input_assist_log")
public class InputAssistLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String page;

    private String field;

    private String triggerType;

    private String question;

    private String answer;

    private Long videoClipId;

    private Boolean videoPlayed;

    private Boolean resolved;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
