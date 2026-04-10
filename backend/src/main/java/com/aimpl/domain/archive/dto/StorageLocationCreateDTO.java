package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StorageLocationCreateDTO {

    @NotBlank(message = "库位编码不能为空")
    @Size(max = 20, message = "库位编码长度不能超过20位")
    private String locationCode;

    @NotBlank(message = "库位名称不能为空")
    @Size(max = 60, message = "库位名称长度不能超过60")
    private String locationName;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Size(max = 20, message = "库位类型长度不能超过20")
    private String locationType;

    private Boolean enabled;
}
