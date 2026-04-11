package com.aimpl.domain.uservideo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user_video")
public class UserVideo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long publisherId;

    private String publisherName;

    private Long projectId;

    private String title;

    private String description;

    private String categoryModule;

    private String videoUrl;

    private Integer videoDuration;

    private String thumbnailUrl;

    private Integer pointsCost;

    private Integer totalViews;

    private Integer totalLearners;

    private Integer publisherEarnedPoints;

    private String approvalStatus;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
