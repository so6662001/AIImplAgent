package com.aimpl.domain.workforce.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyTaskPlanVO {
    private Long engineerId;
    private Long projectId;
    private LocalDate date;
    private String phase;
    private List<TaskItemVO> tasks;
    private List<String> nextDayPrep;
}
