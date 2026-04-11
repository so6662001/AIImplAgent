package com.aimpl.domain.training.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long traineeId;

    private String module;

    private String examType;

    private Integer score;

    private Boolean passed;

    /** 是否必学课程考核 */
    private Boolean requiredCourse;

    private String weakPoints;

    /** 如果是重考，关联原考核ID */
    private Long retryOf;

    private LocalDateTime examTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
