package com.aimpl.domain.golive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoLiveCheckUpdateDTO {

    @NotBlank(message = "检查结果不能为空")
    private String checkResult;

    private String detail;

    private String checkedBy;
}
