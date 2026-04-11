package com.aimpl.domain.qa.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QaSessionVO {

    private Long id;
    private Long projectId;
    private Long clientUserId;
    private String title;
    private String status;
    private Integer messageCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
