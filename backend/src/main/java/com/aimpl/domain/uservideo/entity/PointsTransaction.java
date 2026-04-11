package com.aimpl.domain.uservideo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_points_transaction")
public class PointsTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private String transactionType;

    private Integer amount;

    private Integer balanceBefore;

    private Integer balanceAfter;

    private Long relatedVideoId;

    private String description;

    private Boolean externalApiCalled;

    private String externalApiResponse;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
