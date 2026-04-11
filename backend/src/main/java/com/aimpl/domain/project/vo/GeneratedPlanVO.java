package com.aimpl.domain.project.vo;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedPlanVO {

    private int estimatedDurationDays;
    private String riskLevel;
    private List<MilestoneItem> milestones;
    private List<WbsItem> wbsItems;
    private List<ResourceItem> resourcePlan;
    private List<RiskItem> riskPlan;
    private List<GanttItem> ganttData;

    @Data
    public static class MilestoneItem {
        private String name;
        private int dayOffset;
        private String deliverables;

        public MilestoneItem() {}

        public MilestoneItem(String name, int dayOffset, String deliverables) {
            this.name = name;
            this.dayOffset = dayOffset;
            this.deliverables = deliverables;
        }
    }

    @Data
    public static class WbsItem {
        private String phase;
        private String taskName;
        private int startDay;
        private int endDay;
        private String responsible;
        private String deliverable;

        public WbsItem() {}

        public WbsItem(String phase, String taskName, int startDay, int endDay, String responsible, String deliverable) {
            this.phase = phase;
            this.taskName = taskName;
            this.startDay = startDay;
            this.endDay = endDay;
            this.responsible = responsible;
            this.deliverable = deliverable;
        }
    }

    @Data
    public static class ResourceItem {
        private String role;
        private int headcount;
        private String phase;
        private String skills;

        public ResourceItem() {}

        public ResourceItem(String role, int headcount, String phase, String skills) {
            this.role = role;
            this.headcount = headcount;
            this.phase = phase;
            this.skills = skills;
        }
    }

    @Data
    public static class RiskItem {
        private String riskName;
        private String riskLevel;
        private String impact;
        private String mitigation;
        private boolean highRisk;

        public RiskItem() {}

        public RiskItem(String riskName, String riskLevel, String impact, String mitigation, boolean highRisk) {
            this.riskName = riskName;
            this.riskLevel = riskLevel;
            this.impact = impact;
            this.mitigation = mitigation;
            this.highRisk = highRisk;
        }
    }

    @Data
    public static class GanttItem {
        private String taskName;
        private String phase;
        private int startDay;
        private int endDay;
        private double progress;
        private String dependencies;

        public GanttItem() {}

        public GanttItem(String taskName, String phase, int startDay, int endDay, double progress, String dependencies) {
            this.taskName = taskName;
            this.phase = phase;
            this.startDay = startDay;
            this.endDay = endDay;
            this.progress = progress;
            this.dependencies = dependencies;
        }
    }
}
