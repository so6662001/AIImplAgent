package com.aimpl.domain.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_related_unit")
public class RelatedUnit {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String unitCode;

    private String fullName;

    private String unitType;

    private String creditCode;

    private String contactPerson;

    private String phone;

    private String address;

    private String bankName;

    private String bankAccount;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
