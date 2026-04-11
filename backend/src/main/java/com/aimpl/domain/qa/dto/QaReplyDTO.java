package com.aimpl.domain.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QaReplyDTO {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 5000, message = "回复内容不能超过5000个字符")
    private String content;

    private String relatedModule;

    private String relatedVideoUrl;
}
