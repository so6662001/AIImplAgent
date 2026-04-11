package com.aimpl.domain.qa.dto;

import lombok.Data;

@Data
public class ClientLoginVO {

    private String accessToken;
    private Long projectId;
    private String projectName;
    private String employeeName;
    private String role;
}
