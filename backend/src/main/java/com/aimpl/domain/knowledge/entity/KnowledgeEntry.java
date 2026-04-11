package com.aimpl.domain.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_knowledge_entry")
public class KnowledgeEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String category;

    private String layer;

    private String title;

    private String content;

    private String keywords;

    private String source;

    private Long projectId;

    private String accessLevel;

    private Integer viewCount;

    private Integer helpfulCount;

    private Boolean enabled;

    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
