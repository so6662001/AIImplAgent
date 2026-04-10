package com.aimpl.domain.simulation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockSupplierVO {
    private String code;
    private String name;
    private String type;
    private String contact;
    private String phone;
}
