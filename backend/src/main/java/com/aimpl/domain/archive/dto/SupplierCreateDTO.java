package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierCreateDTO {

    @NotBlank(message = "供应商编码不能为空")
    @Size(max = 32, message = "供应商编码长度不能超过32位")
    private String supplierCode;

    @NotBlank(message = "供应商全称不能为空")
    @Size(max = 120, message = "供应商全称长度不能超过120")
    private String fullName;

    @Size(max = 40, message = "供应商简称长度不能超过40")
    private String shortName;

    @Size(max = 20, message = "供应商类型长度不能超过20")
    private String supplierType;

    @Pattern(regexp = "^$|^[0-9A-Za-z]{18}$", message = "统一社会信用代码必须为18位字母数字")
    private String creditCode;

    @Size(max = 30, message = "联系人长度不能超过30")
    private String contact;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    @Size(max = 20, message = "结算方式长度不能超过20")
    private String settlementMethod;

    @DecimalMin(value = "0", message = "税率不能为负数")
    @DecimalMax(value = "1", message = "税率不能大于1")
    private BigDecimal taxRate;
}
