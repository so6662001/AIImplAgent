package com.aimpl.domain.research.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSectionVO {

    private String sectionTitle;

    private String content;

    private List<String> highlights;
}
