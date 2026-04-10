package com.aimpl.domain.org.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DepartmentCreateDTO {

    @NotBlank(message = "部门编码不能为空")
    @Size(max = 20, message = "部门编码长度不能超过20位")
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 60, message = "部门名称长度不能超过60")
    private String deptName;

    private Long parentId;

    private Long managerId;

    @Min(value = 0, message = "排序值不能为负数")
    @Max(value = 999999, message = "排序值过大")
    private Integer sortOrder;

    private Boolean enabled;
}
