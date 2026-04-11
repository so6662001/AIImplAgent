package com.aimpl.domain.server.dto;

import com.aimpl.common.enums.DbType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ServerProfileCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "服务器名称不能为空")
    @Size(max = 60, message = "服务器名称长度不能超过60")
    private String serverName;

    @NotBlank(message = "主机地址不能为空")
    @Size(max = 200, message = "主机地址长度不能超过200")
    private String host;

    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口号不能小于1")
    @Max(value = 65535, message = "端口号不能大于65535")
    private Integer port;

    private DbType dbType;

    @NotBlank(message = "数据库名称不能为空")
    @Size(max = 60, message = "数据库名称长度不能超过60")
    private String dbName;

    private Boolean sslEnabled;

    @Size(max = 30, message = "操作系统类型长度不能超过30")
    private String osType;

    @Size(max = 30, message = "ERP版本长度不能超过30")
    private String erpVersion;

    @Pattern(regexp = "^(https?://.*)?$", message = "API地址格式不正确")
    @Size(max = 500, message = "API地址长度不能超过500")
    private String apiBaseUrl;
}
