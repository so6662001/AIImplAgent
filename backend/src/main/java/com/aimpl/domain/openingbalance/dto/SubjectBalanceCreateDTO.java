package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubjectBalanceCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    @DecimalMin(value = "0", message = "借方余额不能为负数")
    private BigDecimal debitBalance;

    @DecimalMin(value = "0", message = "贷方余额不能为负数")
    private BigDecimal creditBalance;
}
