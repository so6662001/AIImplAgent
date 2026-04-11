package com.aimpl.domain.inputassist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InputAssistLogDTO {

    private Long userId;

    @NotBlank(message = "页面不能为空")
    private String page;

    private String field;

    @NotBlank(message = "触发类型不能为空")
    private String triggerType;

    private String question;

    private String answer;

    private Long videoClipId;

    private Boolean videoPlayed;

    private Boolean resolved;
}
