package com.aimpl.domain.workforce.vo;

import lombok.Data;

@Data
public class TaskItemVO {
    private String category;
    private String title;
    private String description;
    private String priority;
    private String estimatedTime;
}
