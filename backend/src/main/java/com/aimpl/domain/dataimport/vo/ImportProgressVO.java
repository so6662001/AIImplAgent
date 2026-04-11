package com.aimpl.domain.dataimport.vo;

import lombok.Data;

import java.util.List;

@Data
public class ImportProgressVO {

    private Long projectId;
    private int currentBatch;
    private String overallStatus;
    private List<BatchStatusVO> batches;
    private String lastError;

    @Data
    public static class BatchStatusVO {
        private int batchNumber;
        private String batchName;
        private String status;
        private int itemCount;
        private List<String> items;
    }
}
