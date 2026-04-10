package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarehouseCreateDTO {

    @NotBlank(message = "仓库编码不能为空")
    @Size(max = 20, message = "仓库编码长度不能超过20位")
    private String warehouseCode;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 60, message = "仓库名称长度不能超过60")
    private String warehouseName;

    @Size(max = 20, message = "仓库类型长度不能超过20")
    private String warehouseType;

    @Size(max = 20, message = "仓库性质长度不能超过20")
    private String warehouseNature;

    @Size(max = 20, message = "管理方式长度不能超过20")
    private String managementMode;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @Size(max = 30, message = "联系人长度不能超过30")
    private String contact;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$",
             message = "联系电话格式不正确")
    private String phone;

    @DecimalMin(value = "0", message = "面积不能为负数")
    private BigDecimal areaSqm;

    @Min(value = 0, message = "行车数量不能为负数")
    @Max(value = 9999, message = "行车数量过大")
    private Integer craneCount;

    private Boolean enabled;
}
