package com.aimpl.domain.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BankAccountCreateDTO {

    @NotBlank(message = "账户编码不能为空")
    @Size(max = 20, message = "账户编码长度不能超过20位")
    private String accountCode;

    @NotBlank(message = "账户名称不能为空")
    @Size(max = 60, message = "账户名称长度不能超过60")
    private String accountName;

    @NotBlank(message = "账户类型不能为空")
    private String accountType;

    private String bankName;

    private String bankAccountNo;

    private String bankBranch;

    private String currency;

    private String subjectCode;

    private Boolean enabled;
}
