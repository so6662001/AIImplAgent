package com.aimpl.domain.archive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerCreateDTO {

    @NotBlank(message = "客户编码不能为空")
    @Size(max = 32, message = "客户编码长度不能超过32位")
    private String customerCode;

    @NotBlank(message = "客户全称不能为空")
    @Size(max = 120, message = "客户全称长度不能超过120")
    private String fullName;

    @Size(max = 40, message = "客户简称长度不能超过40")
    private String shortName;

    @Size(max = 20, message = "客户类型长度不能超过20")
    private String customerType;

    @Pattern(regexp = "^$|^[0-9A-Z]{18}$", message = "统一社会信用代码必须为18位字母数字")
    private String creditCode;

    @Size(max = 30, message = "联系人长度不能超过30")
    private String contact;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$|^0\\d{2,3}-?\\d{7,8}$",
             message = "联系电话格式不正确")
    private String phone;

    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @Size(max = 20, message = "城市长度不能超过20")
    private String city;

    @Size(max = 20, message = "区县长度不能超过20")
    private String district;

    @Size(max = 200, message = "详细地址长度不能超过200")
    private String address;

    @Size(max = 30, message = "分类长度不能超过30")
    private String category;

    @Size(max = 20, message = "结算方式长度不能超过20")
    private String settlementMethod;

    @DecimalMin(value = "0", message = "信用额度不能为负数")
    private BigDecimal creditLimit;

    @DecimalMin(value = "0", message = "税率不能为负数")
    @DecimalMax(value = "1", message = "税率不能大于1")
    private BigDecimal taxRate;

    private Long salesRepId;
}
