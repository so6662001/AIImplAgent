package com.aimpl.domain.inputassist.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InputAssistStatsVO {

    private long totalAssists;
    private Map<String, Long> byTriggerType;
    private double resolvedRate;
    private List<FieldCount> topFields;

    @Data
    public static class FieldCount {
        private String field;
        private long count;

        public FieldCount(String field, long count) {
            this.field = field;
            this.count = count;
        }
    }
}
