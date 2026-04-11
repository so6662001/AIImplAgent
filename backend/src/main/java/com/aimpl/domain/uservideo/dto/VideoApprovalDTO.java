package com.aimpl.domain.uservideo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoApprovalDTO {

    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    private String rejectionReason;
}
