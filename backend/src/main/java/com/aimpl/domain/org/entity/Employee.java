package com.aimpl.domain.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String employeeCode;

    private String name;

    private String gender;

    private String idCard;

    private String phone;

    private String email;

    private Long deptId;

    private String position;

    private LocalDate joinDate;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
