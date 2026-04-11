package com.aimpl.domain.training.vo;

import com.aimpl.domain.training.dto.TrainingDashboardDTO;
import lombok.Data;

import java.util.List;

@Data
public class EnhancedDashboardVO {
    private TrainingDashboardDTO basicDashboard;
    private List<TraineeStatusVO> traineeStatuses;
    private List<ModuleTrainingStatusVO> moduleStatuses;
    private List<DayDocumentStatusVO> documentMatrix;
    private List<String> riskWarnings;
}
