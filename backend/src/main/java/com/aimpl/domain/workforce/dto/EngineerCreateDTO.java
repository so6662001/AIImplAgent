package com.aimpl.domain.workforce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EngineerCreateDTO {

    @NotBlank(message = "工程师编号不能为空")
    @Size(max = 20, message = "工程师编号长度不能超过20")
    private String engineerCode;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过30")
    private String name;

    @NotBlank(message = "级别不能为空")
    private String level;

    private String skills;

    private LocalDate joinDate;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;
}
