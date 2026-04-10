package com.aimpl.domain.dispatch.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ScheduleSuggestionVO {

    private LocalDate recommendedStart;
    private int estimatedDurationDays;
    private List<MilestoneVO> milestones;

    @Data
    public static class MilestoneVO {
        private String name;
        private int dayOffset;
        private String deliverables;

        public MilestoneVO() {}

        public MilestoneVO(String name, int dayOffset, String deliverables) {
            this.name = name;
            this.dayOffset = dayOffset;
            this.deliverables = deliverables;
        }
    }
}
