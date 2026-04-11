package com.aimpl.domain.golive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GoLiveCheckUpdateDTO {

    @NotBlank(message = "检查结果不能为空")
    @Size(max = 20, message = "检查结果长度不能超过20")
    private String checkResult;

    @Size(max = 1000, message = "详情长度不能超过1000")
    private String detail;

    @Size(max = 60, message = "检查人长度不能超过60")
    private String checkedBy;
}
