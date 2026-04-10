package com.aimpl.domain.simulation.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MockDataSetVO {
    private Long projectId;
    private LocalDateTime generatedAt;
    private List<MockProductVO> products;
    private List<MockCustomerVO> customers;
    private List<MockSupplierVO> suppliers;
    private List<MockInventoryVO> inventory;
    private List<MockTransactionVO> transactions;
    private Summary summary;

    @Data
    public static class Summary {
        private int totalProducts;
        private int totalCustomers;
        private int totalSuppliers;
        private String totalInventoryValue;
    }
}
