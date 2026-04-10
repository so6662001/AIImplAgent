package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OtherReceivableCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    @NotBlank(message = "对象类型不能为空")
    private String objectType;

    private Long objectId;

    @NotBlank(message = "对象名称不能为空")
    private String objectName;

    private String summary;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额不能小于0.01")
    private BigDecimal amount;

    private LocalDate occurDate;

    private String remark;
}
