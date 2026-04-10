package com.aimpl.domain.qa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientLoginDTO {

    @NotBlank(message = "项目编号不能为空")
    private String projectCode;

    @NotBlank(message = "姓名不能为空")
    private String employeeName;
}
