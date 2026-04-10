package com.aimpl.domain.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AccountSubjectCreateDTO {

    @NotBlank(message = "科目编码不能为空")
    @Size(max = 20, message = "科目编码长度不能超过20位")
    private String subjectCode;

    @NotBlank(message = "科目名称不能为空")
    @Size(max = 60, message = "科目名称长度不能超过60")
    private String subjectName;

    @Size(max = 20, message = "上级科目编码长度不能超过20")
    private String parentCode;

    @NotBlank(message = "科目类别不能为空")
    @Size(max = 20, message = "科目类别长度不能超过20")
    private String subjectCategory;

    @NotBlank(message = "余额方向不能为空")
    @Size(max = 10, message = "余额方向长度不能超过10")
    private String balanceDirection;

    @Size(max = 200, message = "辅助核算长度不能超过200")
    private String auxiliaryAccounting;

    private Boolean isLeaf;

    private Boolean enabled;
}
