package com.aimpl.domain.golive.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class GoLiveCheckCreateDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @NotBlank(message = "检查类别不能为空")
    private String category;

    @NotBlank(message = "检查项名称不能为空")
    @Size(max = 100, message = "检查项名称长度不能超过100")
    private String itemName;

    private String description;
}
