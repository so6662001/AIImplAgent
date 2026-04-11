package com.aimpl.domain.finance.dto;

import jakarta.validation.constraints.*;
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
    @Size(max = 20, message = "账户类型长度不能超过20")
    private String accountType;

    @Size(max = 60, message = "银行名称长度不能超过60")
    private String bankName;

    @Size(max = 40, message = "银行账号长度不能超过40")
    private String bankAccountNo;

    @Size(max = 100, message = "开户行支行长度不能超过100")
    private String bankBranch;

    @Size(max = 10, message = "币种长度不能超过10")
    private String currency;

    @Size(max = 20, message = "科目编码长度不能超过20")
    private String subjectCode;

    private Boolean enabled;
}
