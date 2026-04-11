package com.aimpl.domain.simulation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockProductVO {
    private String name;
    private String spec;
    private String material;
    private String steelMill;
    private String unit;
    private String category;
}
