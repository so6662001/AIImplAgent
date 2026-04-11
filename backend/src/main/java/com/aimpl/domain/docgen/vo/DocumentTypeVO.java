package com.aimpl.domain.docgen.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentTypeVO {

    private String typeCode;
    private String typeName;
    private String description;
    private boolean generated;
    private LocalDateTime lastGeneratedAt;
    private int recordCount;
}
