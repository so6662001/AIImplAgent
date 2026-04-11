package com.aimpl.domain.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QaAskDTO {

    private Long sessionId;

    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题长度不能超过2000个字符")
    private String question;

    private String currentPage;

    private String currentField;
}
