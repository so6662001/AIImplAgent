package com.aimpl.domain.report.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DeliveryReportSectionVO {

    private Integer sectionNumber;

    private String sectionTitle;

    private String content;

    private List<String> highlights;

    private Map<String, Object> dataPoints;
}
