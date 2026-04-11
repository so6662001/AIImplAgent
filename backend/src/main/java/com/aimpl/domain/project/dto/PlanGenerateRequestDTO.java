package com.aimpl.domain.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlanGenerateRequestDTO {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private String customizationLevel;

    private LocalDate deadline;

    private String budget;

    private List<String> integrationRequirements;
}
