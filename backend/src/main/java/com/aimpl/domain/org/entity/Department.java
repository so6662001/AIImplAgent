package com.aimpl.domain.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String deptCode;

    private String deptName;

    private Long parentId;

    private Long managerId;

    private Integer sortOrder;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
