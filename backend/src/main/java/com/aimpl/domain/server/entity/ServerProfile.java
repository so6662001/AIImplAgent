package com.aimpl.domain.server.entity;

import com.aimpl.common.enums.DbType;
import com.aimpl.common.enums.ServerStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_server_profile")
public class ServerProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String serverName;

    private String host;

    private Integer port;

    private DbType dbType;

    private String dbName;

    private Boolean sslEnabled;

    private String osType;

    private String erpVersion;

    private String apiBaseUrl;

    private ServerStatus status;

    private LocalDateTime lastHealthCheck;

    private Integer networkLatencyMs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
