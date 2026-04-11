package com.aimpl.domain.knowledge.vo;

import com.aimpl.domain.knowledge.entity.KnowledgeEntry;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class KnowledgeStatsVO {

    private long totalEntries;

    private Map<String, Long> byCategory;

    private Map<String, Long> byLayer;

    private List<KnowledgeEntry> topViewed;

    private List<KnowledgeEntry> topHelpful;
}
