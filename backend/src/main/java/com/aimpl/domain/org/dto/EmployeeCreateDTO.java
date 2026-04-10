package com.aimpl.domain.org.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeCreateDTO {

    @NotBlank(message = "员工编码不能为空")
    @Size(max = 20, message = "员工编码长度不能超过20位")
    private String employeeCode;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过30")
    private String name;

    private String gender;

    @Pattern(regexp = "^[0-9X]{18}$", message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "部门ID不能为空")
    private Long deptId;

    @Size(max = 30, message = "职位长度不能超过30")
    private String position;

    private LocalDate joinDate;

    private Boolean enabled;
}
