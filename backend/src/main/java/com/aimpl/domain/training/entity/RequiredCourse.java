package com.aimpl.domain.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_required_course")
public class RequiredCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String industryType;

    private String courseModule;

    private Boolean kaRequired;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
