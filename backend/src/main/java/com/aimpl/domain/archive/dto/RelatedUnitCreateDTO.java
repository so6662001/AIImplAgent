package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RelatedUnitCreateDTO {

    @NotBlank(message = "单位编码不能为空")
    @Size(max = 32, message = "单位编码长度不能超过32位")
    private String unitCode;

    @NotBlank(message = "单位全称不能为空")
    @Size(max = 120, message = "单位全称长度不能超过120")
    private String fullName;

    @NotBlank(message = "单位类型不能为空")
    private String unitType;

    @Pattern(regexp = "^$|^[0-9A-Za-z]{18}$", message = "统一社会信用代码必须为18位字母数字")
    private String creditCode;

    @Size(max = 30, message = "联系人长度不能超过30")
    private String contactPerson;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @Size(max = 60, message = "银行名称长度不能超过60")
    private String bankName;

    @Size(max = 40, message = "银行账号长度不能超过40")
    private String bankAccount;
}
