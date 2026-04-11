package com.aimpl.domain.openingbalance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OtherPayableCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "科目编码不能为空")
    @Size(max = 20, message = "科目编码长度不能超过20")
    private String subjectCode;

    @NotBlank(message = "对象类型不能为空")
    @Size(max = 20, message = "对象类型长度不能超过20")
    private String objectType;

    private Long objectId;

    @NotBlank(message = "对象名称不能为空")
    @Size(max = 60, message = "对象名称长度不能超过60")
    private String objectName;

    @Size(max = 200, message = "摘要长度不能超过200")
    private String summary;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额不能小于0.01")
    private BigDecimal amount;

    private LocalDate occurDate;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
