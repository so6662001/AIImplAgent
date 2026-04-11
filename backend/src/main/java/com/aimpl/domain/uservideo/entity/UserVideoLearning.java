package com.aimpl.domain.uservideo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_video_learning")
public class UserVideoLearning {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long videoId;

    private Long learnerId;

    private String learnerName;

    private Integer watchedDuration;

    private Boolean completed;

    private Integer pointsPaid;

    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
