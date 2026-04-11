package com.aimpl.domain.dataimport.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReconciliationResultVO {

    private Long projectId;
    private LocalDateTime checkedAt;
    private boolean allPassed;
    private int passCount;
    private int warnCount;
    private int failCount;
    private List<ReconciliationItemVO> items;

    @Data
    public static class ReconciliationItemVO {
        private String checkName;
        private String leftLabel;
        private BigDecimal leftValue;
        private String rightLabel;
        private BigDecimal rightValue;
        private BigDecimal difference;
        private String result;
        private String remark;
    }
}
