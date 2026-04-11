package com.aimpl.domain.workforce.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompositeScoreVO {

    private Long engineerId;
    private String name;
    private BigDecimal compositeScore;
    private String rating;
    private List<DimensionScoreVO> dimensions;
    private List<String> growthSuggestions;

    @Data
    public static class DimensionScoreVO {
        private String dimensionName;
        private BigDecimal weight;
        private BigDecimal score;
        private String source;
    }
}
