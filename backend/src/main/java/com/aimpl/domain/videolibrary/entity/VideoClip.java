package com.aimpl.domain.videolibrary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_video_clip")
public class VideoClip {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;

    private String clipTitle;

    private Integer startSecond;

    private Integer endSecond;

    private String relatedPage;

    private String relatedField;

    private String operationStep;

    private String subtitleText;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
