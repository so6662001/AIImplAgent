package com.aimpl.domain.org.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    private Integer sortOrder;

    private Boolean enabled;
}
