package com.aimpl.domain.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QaMessageVO {

    private Long id;
    private String role;
    private String content;
    private String relatedModule;
    private String relatedVideoUrl;
    private Boolean helpful;
    private LocalDateTime createTime;
}
