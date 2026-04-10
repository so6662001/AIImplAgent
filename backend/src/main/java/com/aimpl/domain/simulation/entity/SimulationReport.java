package com.aimpl.domain.simulation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_simulation_report")
public class SimulationReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Integer totalScenes;

    private Integer passedScenes;

    private Integer failedScenes;

    private BigDecimal overallPassRate;

    private BigDecimal overallScore;

    private Boolean readyForTraining;

    private String reportContent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
