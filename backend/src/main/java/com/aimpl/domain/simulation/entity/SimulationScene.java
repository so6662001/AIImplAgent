package com.aimpl.domain.simulation.entity;

import com.aimpl.common.enums.SceneType;
import com.aimpl.common.enums.SimulationStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_simulation_scene")
public class SimulationScene {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String sceneName;

    private SceneType sceneType;

    private String description;

    private String steps;

    private String expectedResult;

    private String actualResult;

    private SimulationStatus status;

    private LocalDateTime executedAt;

    private String executedBy;

    private String deviation;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
