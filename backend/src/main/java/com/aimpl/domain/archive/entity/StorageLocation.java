package com.aimpl.domain.archive.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_storage_location")
public class StorageLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String locationCode;

    private String locationName;

    private Long warehouseId;

    private String locationType;

    private Boolean enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
