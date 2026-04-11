package com.aimpl.domain.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeEntryCreateDTO {

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "知识层级不能为空")
    private String layer;

    private String keywords;

    private String source;

    private Long projectId;

    private String accessLevel = "PUBLIC";
}
