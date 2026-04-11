package com.aimpl.domain.assist.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.assist.dto.SupportTicketCreateDTO;
import com.aimpl.domain.assist.entity.SupportTicket;
import com.aimpl.domain.assist.mapper.SupportTicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupportTicketService extends ServiceImpl<SupportTicketMapper, SupportTicket> {

    @Transactional
    public SupportTicket createTicket(SupportTicketCreateDTO dto) {
        String category = dto.getCategory().toUpperCase();
        String level = classifyLevel(category);
        String priority = determinePriority(dto.getTitle(), dto.getDescription());
        String aiSuggestion = generateAiSuggestion(level, dto.getDescription());
        String assignedTo = autoAssign(level);

        SupportTicket ticket = new SupportTicket();
        ticket.setProjectId(dto.getProjectId());
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setCategory(category);
        ticket.setLevel(level);
        ticket.setPriority(priority);
        ticket.setAiSuggestion(aiSuggestion);
        ticket.setAssignedTo(assignedTo);
        ticket.setStatus("OPEN");
        save(ticket);
        return ticket;
    }

    @Transactional
    public SupportTicket resolveTicket(Long id, String resolution) {
        SupportTicket ticket = getById(id);
        if (ticket == null) {
            throw new BizException("工单不存在: " + id);
        }
        ticket.setStatus("RESOLVED");
        ticket.setResolution(resolution);
        ticket.setResolvedAt(LocalDateTime.now());
        updateById(ticket);
        return ticket;
    }

    public List<SupportTicket> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<SupportTicket>()
                .eq(SupportTicket::getProjectId, projectId)
                .orderByDesc(SupportTicket::getCreateTime));
    }

    public Map<String, Object> getTicketStats(Long projectId) {
        List<SupportTicket> tickets = listByProject(projectId);

        long total = tickets.size();
        long open = tickets.stream().filter(t -> "OPEN".equals(t.getStatus()) || "IN_PROGRESS".equals(t.getStatus())).count();
        long resolved = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus())).count();

        Map<String, Long> byLevel = new HashMap<>();
        byLevel.put("L1", tickets.stream().filter(t -> "L1".equals(t.getLevel())).count());
        byLevel.put("L2", tickets.stream().filter(t -> "L2".equals(t.getLevel())).count());
        byLevel.put("L3", tickets.stream().filter(t -> "L3".equals(t.getLevel())).count());
        byLevel.put("L4", tickets.stream().filter(t -> "L4".equals(t.getLevel())).count());

        Map<String, Long> byCategory = new HashMap<>();
        byCategory.put("OPERATION", tickets.stream().filter(t -> "OPERATION".equals(t.getCategory())).count());
        byCategory.put("CONFIG", tickets.stream().filter(t -> "CONFIG".equals(t.getCategory())).count());
        byCategory.put("PROCESS", tickets.stream().filter(t -> "PROCESS".equals(t.getCategory())).count());
        byCategory.put("BUG", tickets.stream().filter(t -> "BUG".equals(t.getCategory())).count());

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("open", open);
        stats.put("resolved", resolved);
        stats.put("byLevel", byLevel);
        stats.put("byCategory", byCategory);
        return stats;
    }

    private String classifyLevel(String category) {
        return switch (category) {
            case "OPERATION" -> "L1";
            case "CONFIG" -> "L2";
            case "PROCESS" -> "L3";
            case "BUG" -> "L4";
            default -> "L2";
        };
    }

    private String determinePriority(String title, String description) {
        String text = (title + " " + description).toLowerCase();
        if (text.contains("紧急") || text.contains("系统不可用") || text.contains("无法登录")) {
            return "URGENT";
        }
        if (text.contains("错误") || text.contains("报错") || text.contains("异常")) {
            return "HIGH";
        }
        if (text.contains("建议") || text.contains("优化")) {
            return "LOW";
        }
        return "MEDIUM";
    }

    private String generateAiSuggestion(String level, String description) {
        return switch (level) {
            case "L1" -> generateL1Suggestion(description);
            case "L2" -> "建议检查以下配置项：" + extractConfigHints(description);
            case "L3" -> "此问题涉及业务流程调整，建议转交项目经理处理。初步分析：" + summarize(description);
            case "L4" -> "已记录系统异常信息，建议转交研发团队排查。错误描述：" + summarize(description);
            default -> "已记录您的问题，将尽快安排处理。";
        };
    }

    private String generateL1Suggestion(String description) {
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
        return "感谢您的提问，AI助手已记录您的问题。请参考系统帮助文档，或等待人工回复。";
    }

    private String extractConfigHints(String description) {
        StringBuilder hints = new StringBuilder();
        if (description.contains("打印")) hints.append("打印模板配置、");
        if (description.contains("权限")) hints.append("用户权限配置、");
        if (description.contains("审批")) hints.append("审批流程配置、");
        if (description.contains("价格")) hints.append("价格策略配置、");
        if (description.contains("税率")) hints.append("税率参数配置、");
        if (hints.length() == 0) hints.append("系统参数配置、基础数据配置、");
        hints.setLength(hints.length() - 1);
        return hints.toString();
    }

    private String summarize(String description) {
        if (description.length() > 100) {
            return description.substring(0, 100) + "...";
        }
        return description;
    }

    private String autoAssign(String level) {
        return switch (level) {
            case "L1" -> "AI助手";
            case "L2" -> "实施工程师";
            case "L3" -> "项目经理";
            case "L4" -> "研发团队";
            default -> "实施工程师";
        };
    }
}
