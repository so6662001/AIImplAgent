package com.aimpl.domain.aftersales.service;

import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.aftersales.dto.AfterSalesTicketCreateDTO;
import com.aimpl.domain.aftersales.entity.AfterSalesTicket;
import com.aimpl.domain.aftersales.mapper.AfterSalesTicketMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AfterSalesTicketService extends ServiceImpl<AfterSalesTicketMapper, AfterSalesTicket> {

    private final EngineerMapper engineerMapper;

    @Transactional
    public AfterSalesTicket createTicket(AfterSalesTicketCreateDTO dto) {
        String intentType = dto.getIntentType().toUpperCase();
        String channel = dto.getChannel().toUpperCase();

        String slaPriority = classifySlaPriority(intentType, dto.getTitle(), dto.getDescription());
        LocalDateTime slaDeadline = calculateSlaDeadline(slaPriority);

        AfterSalesTicket ticket = new AfterSalesTicket();
        ticket.setProjectId(dto.getProjectId());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setChannel(channel);
        ticket.setIntentType(intentType);
        ticket.setReporterName(dto.getReporterName());
        ticket.setReporterContact(dto.getReporterContact());
        ticket.setSlaPriority(slaPriority);
        ticket.setSlaDeadline(slaDeadline);
        ticket.setKnowledgeCreated(false);

        Engineer assigned = autoAssignEngineer();
        if (assigned != null) {
            ticket.setAssignedEngineerId(assigned.getId());
            ticket.setAssignedEngineerName(assigned.getName());
        }

        if ("CONSULT".equals(intentType)) {
            String autoAnswer = generateConsultAnswer(dto.getDescription());
            ticket.setResolution(autoAnswer);
            ticket.setStatus("RESOLVED");
            ticket.setResolvedAt(LocalDateTime.now());
        } else {
            ticket.setStatus(assigned != null ? "ASSIGNED" : "NEW");
        }

        save(ticket);
        return ticket;
    }

    @Transactional
    public AfterSalesTicket resolveTicket(Long id, String resolution, boolean addToKnowledge) {
        AfterSalesTicket ticket = getById(id);
        if (ticket == null) {
            throw new BizException("售后工单不存在: " + id);
        }
        ticket.setStatus("RESOLVED");
        ticket.setResolution(resolution);
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setKnowledgeCreated(addToKnowledge);
        updateById(ticket);
        return ticket;
    }

    @Transactional
    public void rateTicket(Long id, int satisfaction) {
        AfterSalesTicket ticket = getById(id);
        if (ticket == null) {
            throw new BizException("售后工单不存在: " + id);
        }
        if (satisfaction < 1 || satisfaction > 5) {
            throw new BizException("满意度评分范围为1-5");
        }
        ticket.setCustomerSatisfaction(satisfaction);
        updateById(ticket);
    }

    public List<AfterSalesTicket> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<AfterSalesTicket>()
                .eq(AfterSalesTicket::getProjectId, projectId)
                .orderByDesc(AfterSalesTicket::getCreateTime));
    }

    public Map<String, Object> getStats(Long projectId) {
        List<AfterSalesTicket> tickets = listByProject(projectId);

        long total = tickets.size();
        Map<String, Long> bySla = new HashMap<>();
        bySla.put("P0", tickets.stream().filter(t -> "P0".equals(t.getSlaPriority())).count());
        bySla.put("P1", tickets.stream().filter(t -> "P1".equals(t.getSlaPriority())).count());
        bySla.put("P2", tickets.stream().filter(t -> "P2".equals(t.getSlaPriority())).count());
        bySla.put("P3", tickets.stream().filter(t -> "P3".equals(t.getSlaPriority())).count());

        Map<String, Long> byIntent = new HashMap<>();
        byIntent.put("CONSULT", tickets.stream().filter(t -> "CONSULT".equals(t.getIntentType())).count());
        byIntent.put("FAULT", tickets.stream().filter(t -> "FAULT".equals(t.getIntentType())).count());
        byIntent.put("SUGGESTION", tickets.stream().filter(t -> "SUGGESTION".equals(t.getIntentType())).count());
        byIntent.put("COMPLAINT", tickets.stream().filter(t -> "COMPLAINT".equals(t.getIntentType())).count());

        OptionalDouble avgResolution = tickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreateTime() != null)
                .mapToDouble(t -> Duration.between(t.getCreateTime(), t.getResolvedAt()).toMinutes() / 60.0)
                .average();

        OptionalDouble avgSatisfaction = tickets.stream()
                .filter(t -> t.getCustomerSatisfaction() != null)
                .mapToInt(AfterSalesTicket::getCustomerSatisfaction)
                .average();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("bySla", bySla);
        stats.put("byIntent", byIntent);
        stats.put("avgResolutionHours", avgResolution.isPresent()
                ? BigDecimal.valueOf(avgResolution.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
        stats.put("avgSatisfaction", avgSatisfaction.isPresent()
                ? BigDecimal.valueOf(avgSatisfaction.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
        return stats;
    }

    String classifySlaPriority(String intentType, String title, String description) {
        String text = (title + " " + description).toLowerCase();
        if ("FAULT".equals(intentType)) {
            if (text.contains("系统不可用") || text.contains("登录不了") || text.contains("全部报错")) {
                return "P0";
            }
            return "P1";
        }
        if ("COMPLAINT".equals(intentType)) {
            return "P1";
        }
        if ("CONSULT".equals(intentType) || "SUGGESTION".equals(intentType)) {
            return "P2";
        }
        return "P3";
    }

    LocalDateTime calculateSlaDeadline(String slaPriority) {
        LocalDateTime now = LocalDateTime.now();
        return switch (slaPriority) {
            case "P0" -> now.plusHours(1);
            case "P1" -> now.plusHours(4);
            case "P2" -> now.plusHours(24);
            case "P3" -> now.plusHours(72);
            default -> now.plusHours(72);
        };
    }

    private Engineer autoAssignEngineer() {
        List<Engineer> idleEngineers = engineerMapper.selectList(
                new LambdaQueryWrapper<Engineer>()
                        .eq(Engineer::getCurrentStatus, EngineerStatus.IDLE)
                        .orderByDesc(Engineer::getCompositeScore));
        if (!idleEngineers.isEmpty()) {
            return idleEngineers.get(0);
        }

        List<Engineer> allEngineers = engineerMapper.selectList(
                new LambdaQueryWrapper<Engineer>()
                        .orderByAsc(Engineer::getMonthlyProjectCount));
        if (!allEngineers.isEmpty()) {
            return allEngineers.get(0);
        }

        return null;
    }

    private String generateConsultAnswer(String description) {
        if (description.contains("规格")) {
            return "规格的填写格式通常为：品名+材质+规格尺寸。例如：热轧卷板 Q235B 5.75×1500×C。请确保按照系统要求的格式录入。";
        }
        if (description.contains("入库")) {
            return "采购入库流程：进入【采购管理】→【采购入库单】，选择供应商和仓库，录入商品明细后保存并审核。";
        }
        if (description.contains("出库")) {
            return "销售出库流程：进入【销售管理】→【销售出库单】，选择客户和出库仓库，录入出库商品明细后保存并审核。";
        }
        if (description.contains("盘点")) {
            return "库存盘点流程：进入【库存管理】→【库存盘点】，选择盘点仓库，录入实盘数量，系统自动计算盘盈盘亏。";
        }
        if (description.contains("登录")) {
            return "请确认用户名和密码是否正确，如忘记密码请联系管理员重置。";
        }
        return "感谢您的咨询，AI助手已记录您的问题。请参考系统帮助文档，或等待人工回复。";
    }
}
