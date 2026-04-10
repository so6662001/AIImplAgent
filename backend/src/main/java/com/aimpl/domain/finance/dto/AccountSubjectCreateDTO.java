package com.aimpl.domain.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountSubjectCreateDTO {

    @NotBlank(message = "科目编码不能为空")
    @Size(max = 20, message = "科目编码长度不能超过20位")
    private String subjectCode;

    @NotBlank(message = "科目名称不能为空")
    @Size(max = 60, message = "科目名称长度不能超过60")
    private String subjectName;

    private String parentCode;

    @NotBlank(message = "科目类别不能为空")
    private String subjectCategory;

    @NotBlank(message = "余额方向不能为空")
    private String balanceDirection;

    private String auxiliaryAccounting;

    private Boolean isLeaf;

    private Boolean enabled;
}
