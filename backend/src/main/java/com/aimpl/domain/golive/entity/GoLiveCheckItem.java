package com.aimpl.domain.golive.entity;

import com.aimpl.common.enums.CheckCategory;
import com.aimpl.common.enums.CheckResult;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_go_live_check_item")
public class GoLiveCheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private CheckCategory category;

    private String itemName;

    private String description;

    private CheckResult checkResult;

    private String detail;

    private LocalDateTime checkedAt;

    private String checkedBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
