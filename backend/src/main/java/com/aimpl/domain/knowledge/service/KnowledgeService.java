package com.aimpl.domain.knowledge.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.aftersales.entity.AfterSalesTicket;
import com.aimpl.domain.aftersales.mapper.AfterSalesTicketMapper;
import com.aimpl.domain.knowledge.dto.KnowledgeEntryCreateDTO;
import com.aimpl.domain.knowledge.entity.KnowledgeEntry;
import com.aimpl.domain.knowledge.mapper.KnowledgeEntryMapper;
import com.aimpl.domain.knowledge.vo.KnowledgeStatsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService extends ServiceImpl<KnowledgeEntryMapper, KnowledgeEntry> {

    private static final Set<String> VALID_CATEGORIES =
            Set.of("GENERAL", "INDUSTRY", "EXPERIENCE", "PROJECT_PRIVATE");

    private final AfterSalesTicketMapper afterSalesTicketMapper;

    @Transactional
    public KnowledgeEntry create(KnowledgeEntryCreateDTO dto) {
        String category = dto.getCategory().toUpperCase();
        if (!VALID_CATEGORIES.contains(category)) {
            throw new BizException("无效的知识分类: " + dto.getCategory());
        }
        if ("PROJECT_PRIVATE".equals(category) && dto.getProjectId() == null) {
            throw new BizException("项目私有知识必须指定projectId");
        }

        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setCategory(category);
        entry.setLayer(dto.getLayer());
        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setKeywords(dto.getKeywords());
        entry.setSource(dto.getSource());
        entry.setProjectId(dto.getProjectId());
        entry.setAccessLevel(dto.getAccessLevel() != null ? dto.getAccessLevel() : "PUBLIC");
        entry.setViewCount(0);
        entry.setHelpfulCount(0);
        entry.setEnabled(true);
        save(entry);
        return entry;
    }

    public KnowledgeEntry update(Long id, KnowledgeEntryCreateDTO dto) {
        KnowledgeEntry entry = getById(id);
        if (entry == null) {
            throw new BizException("知识条目不存在: " + id);
        }
        if (dto.getCategory() != null) {
            String category = dto.getCategory().toUpperCase();
            if (!VALID_CATEGORIES.contains(category)) {
                throw new BizException("无效的知识分类: " + dto.getCategory());
            }
            entry.setCategory(category);
        }
        if (dto.getLayer() != null) entry.setLayer(dto.getLayer());
        if (dto.getTitle() != null) entry.setTitle(dto.getTitle());
        if (dto.getContent() != null) entry.setContent(dto.getContent());
        if (dto.getKeywords() != null) entry.setKeywords(dto.getKeywords());
        if (dto.getSource() != null) entry.setSource(dto.getSource());
        if (dto.getProjectId() != null) entry.setProjectId(dto.getProjectId());
        if (dto.getAccessLevel() != null) entry.setAccessLevel(dto.getAccessLevel());
        updateById(entry);
        return entry;
    }

    public List<KnowledgeEntry> listEntries(String category, String layer, Long projectId, String query) {
        LambdaQueryWrapper<KnowledgeEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeEntry::getEnabled, true);

        if (category != null && !category.isBlank()) {
            wrapper.eq(KnowledgeEntry::getCategory, category.toUpperCase());
        }
        if (layer != null && !layer.isBlank()) {
            wrapper.eq(KnowledgeEntry::getLayer, layer);
        }
        if (projectId != null) {
            wrapper.and(w -> w
                    .ne(KnowledgeEntry::getAccessLevel, "PROJECT_ONLY")
                    .or()
                    .eq(KnowledgeEntry::getProjectId, projectId));
        }
        if (query != null && !query.isBlank()) {
            wrapper.and(w -> w
                    .like(KnowledgeEntry::getTitle, query)
                    .or()
                    .like(KnowledgeEntry::getKeywords, query)
                    .or()
                    .like(KnowledgeEntry::getContent, query));
        }
        wrapper.orderByDesc(KnowledgeEntry::getHelpfulCount);
        wrapper.orderByDesc(KnowledgeEntry::getViewCount);
        return list(wrapper);
    }

    public List<KnowledgeEntry> search(String query, String category, Long projectId) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String[] terms = query.trim().split("[,，\\s]+");

        List<KnowledgeEntry> allEnabled = list(new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getEnabled, true));

        if (projectId == null) {
            allEnabled = allEnabled.stream()
                    .filter(e -> !"PROJECT_ONLY".equals(e.getAccessLevel()))
                    .collect(Collectors.toList());
        } else {
            allEnabled = allEnabled.stream()
                    .filter(e -> !"PROJECT_ONLY".equals(e.getAccessLevel())
                            || projectId.equals(e.getProjectId()))
                    .collect(Collectors.toList());
        }

        if (category != null && !category.isBlank()) {
            String cat = category.toUpperCase();
            allEnabled = allEnabled.stream()
                    .filter(e -> cat.equals(e.getCategory()))
                    .collect(Collectors.toList());
        }

        Map<Long, Integer> scoreMap = new LinkedHashMap<>();

        for (KnowledgeEntry entry : allEnabled) {
            int score = 0;
            String keywords = entry.getKeywords() != null ? entry.getKeywords().toLowerCase() : "";
            String title = entry.getTitle() != null ? entry.getTitle().toLowerCase() : "";
            String content = entry.getContent() != null ? entry.getContent().toLowerCase() : "";
            String contentLimited = content.length() > 500 ? content.substring(0, 500) : content;

            for (String term : terms) {
                String t = term.toLowerCase();
                if (t.isBlank()) continue;

                if (keywords.contains(t)) {
                    score += 10;
                }
                if (title.contains(t)) {
                    score += 5;
                }
                if (contentLimited.contains(t)) {
                    score += 2;
                }
            }

            score += (entry.getHelpfulCount() != null ? entry.getHelpfulCount() : 0);

            if (score > 0) {
                scoreMap.put(entry.getId(), score);
            }
        }

        return allEnabled.stream()
                .filter(e -> scoreMap.containsKey(e.getId()))
                .sorted((a, b) -> scoreMap.get(b.getId()) - scoreMap.get(a.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViewCount(Long id) {
        KnowledgeEntry entry = getById(id);
        if (entry != null) {
            entry.setViewCount((entry.getViewCount() != null ? entry.getViewCount() : 0) + 1);
            updateById(entry);
        }
    }

    @Transactional
    public void markHelpful(Long id) {
        KnowledgeEntry entry = getById(id);
        if (entry == null) {
            throw new BizException("知识条目不存在: " + id);
        }
        entry.setHelpfulCount((entry.getHelpfulCount() != null ? entry.getHelpfulCount() : 0) + 1);
        updateById(entry);
    }

    public KnowledgeStatsVO getStats() {
        List<KnowledgeEntry> all = list(new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getEnabled, true));

        KnowledgeStatsVO stats = new KnowledgeStatsVO();
        stats.setTotalEntries(all.size());

        stats.setByCategory(all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory() != null ? e.getCategory() : "UNKNOWN",
                        Collectors.counting())));

        stats.setByLayer(all.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getLayer() != null ? e.getLayer() : "UNKNOWN",
                        Collectors.counting())));

        stats.setTopViewed(all.stream()
                .sorted(Comparator.comparingInt((KnowledgeEntry e) ->
                        e.getViewCount() != null ? e.getViewCount() : 0).reversed())
                .limit(5)
                .collect(Collectors.toList()));

        stats.setTopHelpful(all.stream()
                .sorted(Comparator.comparingInt((KnowledgeEntry e) ->
                        e.getHelpfulCount() != null ? e.getHelpfulCount() : 0).reversed())
                .limit(5)
                .collect(Collectors.toList()));

        return stats;
    }

    @Transactional
    public KnowledgeEntry createFromResolution(Long ticketId) {
        AfterSalesTicket ticket = afterSalesTicketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BizException("售后工单不存在: " + ticketId);
        }
        if (ticket.getResolution() == null || ticket.getResolution().isBlank()) {
            throw new BizException("工单尚未有解决方案");
        }

        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setCategory("EXPERIENCE");
        entry.setLayer("问题方案");
        entry.setTitle(ticket.getTitle());
        entry.setContent("问题描述: " + ticket.getDescription() + "\n\n解决方案: " + ticket.getResolution());
        entry.setKeywords(ticket.getIntentType());
        entry.setSource("售后工单#" + ticketId);
        entry.setProjectId(ticket.getProjectId());
        entry.setAccessLevel("PUBLIC");
        entry.setViewCount(0);
        entry.setHelpfulCount(0);
        entry.setEnabled(true);
        save(entry);
        return entry;
    }
}
