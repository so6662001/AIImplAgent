package com.aimpl.domain.qa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_client_user")
public class ClientUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String employeeName;

    private String role;

    private String department;

    private String accessToken;

    private Boolean enabled;

    private LocalDateTime lastActiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
