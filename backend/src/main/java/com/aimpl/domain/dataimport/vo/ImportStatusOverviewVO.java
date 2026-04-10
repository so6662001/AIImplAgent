package com.aimpl.domain.dataimport.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ImportStatusOverviewVO {

    private Long projectId;
    private int totalItems;
    private int completedItems;
    private BigDecimal completionRate;
    private String overallStatus;
    private List<ImportItemStatusVO> items;
    private ReconciliationResultVO reconciliation;

    @Data
    public static class ImportItemStatusVO {
        private String category;
        private String itemCode;
        private String itemName;
        private int recordCount;
        private String status;
        private int batchNumber;
    }
}
