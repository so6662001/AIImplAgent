package com.aimpl.domain.docgen.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GeneratedDocumentVO {

    private String documentType;
    private String documentName;
    private Long projectId;
    private LocalDateTime generatedAt;
    private String content;
    private List<String> sections;
    private String status;
}
