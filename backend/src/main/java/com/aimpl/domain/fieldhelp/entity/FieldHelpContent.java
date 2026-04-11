package com.aimpl.domain.fieldhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_field_help_content")
public class FieldHelpContent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String page;

    private String fieldName;

    private String helpText;

    private String formatExample;

    private String commonErrors;

    private Long relatedVideoClipId;

    private Integer sortOrder;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
