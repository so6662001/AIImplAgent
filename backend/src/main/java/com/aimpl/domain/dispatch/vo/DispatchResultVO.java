package com.aimpl.domain.dispatch.vo;

import lombok.Data;

import java.util.List;

@Data
public class DispatchResultVO {

    private List<PmRecommendationVO> recommendedPms;
    private ScheduleSuggestionVO scheduleSuggestion;
}
