package com.aimpl.domain.aftersales.service;

import com.aimpl.domain.aftersales.entity.AfterSalesTicket;
import com.aimpl.domain.aftersales.entity.CustomerHealthRecord;
import com.aimpl.domain.aftersales.mapper.CustomerHealthRecordMapper;
import com.aimpl.domain.aftersales.vo.CustomerHealthVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class CustomerHealthService extends ServiceImpl<CustomerHealthRecordMapper, CustomerHealthRecord> {

    private final AfterSalesTicketService afterSalesTicketService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CustomerHealthVO checkHealth(Long projectId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        List<AfterSalesTicket> allTickets = afterSalesTicketService.listByProject(projectId);

        long ticketCount30d = allTickets.stream()
                .filter(t -> t.getCreateTime() != null && t.getCreateTime().isAfter(thirtyDaysAgo))
                .count();

        long ticketCountPrev30d = allTickets.stream()
                .filter(t -> t.getCreateTime() != null
                        && t.getCreateTime().isAfter(sixtyDaysAgo)
                        && t.getCreateTime().isBefore(thirtyDaysAgo))
                .count();

        String ticketTrend = determineTrend(ticketCount30d, ticketCountPrev30d);

        long openTicketCount = allTickets.stream()
                .filter(t -> !"RESOLVED".equals(t.getStatus()) && !"CLOSED".equals(t.getStatus()))
                .count();

        OptionalDouble avgResOpt = allTickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreateTime() != null)
                .mapToDouble(t -> Duration.between(t.getCreateTime(), t.getResolvedAt()).toMinutes() / 60.0)
                .average();
        BigDecimal avgResolutionHours = avgResOpt.isPresent()
                ? BigDecimal.valueOf(avgResOpt.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        OptionalDouble avgSatOpt = allTickets.stream()
                .filter(t -> t.getCustomerSatisfaction() != null)
                .mapToInt(AfterSalesTicket::getCustomerSatisfaction)
                .average();
        BigDecimal customerSatisfactionAvg = avgSatOpt.isPresent()
                ? BigDecimal.valueOf(avgSatOpt.getAsDouble()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        int healthScore = calculateHealthScore((int) openTicketCount, ticketTrend,
                customerSatisfactionAvg, avgResolutionHours);
        String healthLevel = determineHealthLevel(healthScore);

        List<String> careActions = generateCareActions(ticketTrend, (int) openTicketCount,
                customerSatisfactionAvg, ticketCount30d);

        CustomerHealthRecord record = new CustomerHealthRecord();
        record.setProjectId(projectId);
        record.setCheckDate(LocalDate.now());
        record.setTicketCount30d((int) ticketCount30d);
        record.setTicketTrend(ticketTrend);
        record.setOpenTicketCount((int) openTicketCount);
        record.setAvgResolutionHours(avgResolutionHours);
        record.setCustomerSatisfactionAvg(customerSatisfactionAvg);
        record.setHealthScore(healthScore);
        record.setHealthLevel(healthLevel);
        record.setCareActions(serializeCareActions(careActions));
        save(record);

        return toVO(record, careActions);
    }

    public CustomerHealthRecord getLatest(Long projectId) {
        List<CustomerHealthRecord> records = list(new LambdaQueryWrapper<CustomerHealthRecord>()
                .eq(CustomerHealthRecord::getProjectId, projectId)
                .orderByDesc(CustomerHealthRecord::getCheckDate)
                .last("LIMIT 1"));
        return records.isEmpty() ? null : records.get(0);
    }

    public List<CustomerHealthRecord> getHistory(Long projectId) {
        return list(new LambdaQueryWrapper<CustomerHealthRecord>()
                .eq(CustomerHealthRecord::getProjectId, projectId)
                .orderByDesc(CustomerHealthRecord::getCheckDate));
    }

    String determineTrend(long current, long previous) {
        if (previous == 0 && current == 0) {
            return "STABLE";
        }
        if (previous == 0) {
            return "UP";
        }
        double ratio = (double) current / previous;
        if (ratio > 1.2) {
            return "UP";
        } else if (ratio < 0.8) {
            return "DOWN";
        }
        return "STABLE";
    }

    int calculateHealthScore(int openTicketCount, String ticketTrend,
                             BigDecimal avgSatisfaction, BigDecimal avgResolutionHours) {
        int score = 100;
        score -= openTicketCount * 5;
        if ("UP".equals(ticketTrend)) {
            score -= 10;
        }
        if (avgSatisfaction.compareTo(BigDecimal.valueOf(3)) < 0
                && avgSatisfaction.compareTo(BigDecimal.ZERO) > 0) {
            score -= 20;
        } else if (avgSatisfaction.compareTo(BigDecimal.valueOf(4)) < 0
                && avgSatisfaction.compareTo(BigDecimal.ZERO) > 0) {
            score -= 10;
        }
        if (avgResolutionHours.compareTo(BigDecimal.valueOf(72)) > 0) {
            score -= 20;
        } else if (avgResolutionHours.compareTo(BigDecimal.valueOf(24)) > 0) {
            score -= 10;
        }
        return Math.max(score, 0);
    }

    String determineHealthLevel(int score) {
        if (score >= 80) return "HEALTHY";
        if (score >= 60) return "ATTENTION";
        if (score >= 40) return "WARNING";
        return "CRITICAL";
    }

    List<String> generateCareActions(String ticketTrend, int openTicketCount,
                                     BigDecimal avgSatisfaction, long ticketCount30d) {
        List<String> actions = new ArrayList<>();
        if ("UP".equals(ticketTrend)) {
            actions.add("工单数量上升趋势，建议安排客户回访");
        }
        if (openTicketCount > 3) {
            actions.add("存在" + openTicketCount + "个未解决工单，建议优先处理");
        }
        if (avgSatisfaction.compareTo(BigDecimal.valueOf(3)) < 0
                && avgSatisfaction.compareTo(BigDecimal.ZERO) > 0) {
            actions.add("客户满意度较低，建议安排客户成功经理介入");
        }
        if (ticketCount30d == 0) {
            actions.add("近期无互动，建议主动回访了解使用情况");
        }
        return actions;
    }

    private String serializeCareActions(List<String> actions) {
        try {
            return objectMapper.writeValueAsString(actions);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> deserializeCareActions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public CustomerHealthVO toVO(CustomerHealthRecord record, List<String> careActions) {
        CustomerHealthVO vo = new CustomerHealthVO();
        vo.setHealthScore(record.getHealthScore());
        vo.setHealthLevel(record.getHealthLevel());
        vo.setTicketCount30d(record.getTicketCount30d());
        vo.setTicketTrend(record.getTicketTrend());
        vo.setOpenTicketCount(record.getOpenTicketCount());
        vo.setAvgResolutionHours(record.getAvgResolutionHours());
        vo.setCustomerSatisfactionAvg(record.getCustomerSatisfactionAvg());
        vo.setCareActions(careActions);
        vo.setCheckDate(record.getCheckDate());
        return vo;
    }

    public CustomerHealthVO toVO(CustomerHealthRecord record) {
        return toVO(record, deserializeCareActions(record.getCareActions()));
    }
}
