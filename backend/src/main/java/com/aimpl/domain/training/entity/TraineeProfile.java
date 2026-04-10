package com.aimpl.domain.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_trainee_profile")
public class TraineeProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long employeeId;

    private String employeeName;

    private String role;

    private String department;

    /** 是否KA用户 */
    private Boolean kaUser;

    private Integer progressPercent;

    private Integer attendanceDays;

    private Integer totalDays;

    private String riskLevel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
