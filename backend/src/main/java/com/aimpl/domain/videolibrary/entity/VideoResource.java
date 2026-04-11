package com.aimpl.domain.videolibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_video_resource")
public class VideoResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String functionName;

    private String title;

    private String description;

    private String videoUrl;

    private Integer duration;

    private Integer sortOrder;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
