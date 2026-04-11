package com.aimpl.domain.golive.service;

import com.aimpl.common.enums.CheckCategory;
import com.aimpl.common.enums.CheckResult;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.golive.dto.GoLiveCheckUpdateDTO;
import com.aimpl.domain.golive.entity.GoLiveCheckItem;
import com.aimpl.domain.golive.mapper.GoLiveCheckItemMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoLiveCheckService extends ServiceImpl<GoLiveCheckItemMapper, GoLiveCheckItem> {

    private final ProjectMapper projectMapper;

    private static final List<String[]> STANDARD_CHECKLIST = List.of(
            new String[]{"DATA", "基础数据导入完整性", "检查客户、供应商、物料等基础数据是否完整导入"},
            new String[]{"DATA", "历史数据迁移验证", "检查历史业务数据迁移是否正确"},
            new String[]{"DATA", "期初余额核对", "检查期初余额是否与原系统一致"},
            new String[]{"CONFIG", "帐套参数配置", "检查帐套核算参数是否正确配置"},
            new String[]{"CONFIG", "审批流程配置", "检查各业务单据审批流程是否配置完毕"},
            new String[]{"CONFIG", "权限角色配置", "检查用户权限和角色是否正确分配"},
            new String[]{"FUNCTION", "采购流程验证", "验证采购入库全流程是否正常运行"},
            new String[]{"FUNCTION", "销售流程验证", "验证销售出库全流程是否正常运行"},
            new String[]{"FUNCTION", "库存管理验证", "验证库存盘点、调拨等功能是否正常"},
            new String[]{"FUNCTION", "财务结算验证", "验证财务结算与对账功能是否正常"},
            new String[]{"FUNCTION", "报表输出验证", "验证各类统计报表是否正确输出"},
            new String[]{"TRAINING", "关键用户培训完成", "检查所有KA用户是否完成培训并通过考核"},
            new String[]{"TRAINING", "操作手册发放", "检查操作手册是否已发放到位"},
            new String[]{"PERSONNEL", "上线支持团队到位", "确认上线期间的现场支持人员已安排"},
            new String[]{"PERSONNEL", "应急预案准备", "确认系统故障应急预案已准备就绪"}
    );

    @Transactional
    public List<GoLiveCheckItem> initChecklist(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        long existing = count(new LambdaQueryWrapper<GoLiveCheckItem>()
                .eq(GoLiveCheckItem::getProjectId, projectId));
        if (existing > 0) {
            throw new BizException("该项目已初始化上线检查清单");
        }

        List<GoLiveCheckItem> items = new ArrayList<>();
        for (String[] template : STANDARD_CHECKLIST) {
            GoLiveCheckItem item = new GoLiveCheckItem();
            item.setProjectId(projectId);
            item.setCategory(CheckCategory.valueOf(template[0]));
            item.setItemName(template[1]);
            item.setDescription(template[2]);
            item.setCheckResult(CheckResult.UNCHECKED);
            items.add(item);
        }
        saveBatch(items);
        return items;
    }

    public List<GoLiveCheckItem> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<GoLiveCheckItem>()
                .eq(GoLiveCheckItem::getProjectId, projectId)
                .orderByAsc(GoLiveCheckItem::getId));
    }

    @Transactional
    public GoLiveCheckItem updateCheck(Long id, GoLiveCheckUpdateDTO dto) {
        GoLiveCheckItem item = getById(id);
        if (item == null) {
            throw new BizException("检查项不存在: " + id);
        }

        CheckResult result;
        try {
            result = CheckResult.valueOf(dto.getCheckResult());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的检查结果: " + dto.getCheckResult());
        }

        item.setCheckResult(result);
        item.setDetail(dto.getDetail());
        item.setCheckedBy(dto.getCheckedBy());
        item.setCheckedAt(LocalDateTime.now());
        updateById(item);
        return item;
    }

    public Map<String, Object> getReadiness(Long projectId) {
        List<GoLiveCheckItem> items = listByProjectId(projectId);
        if (items.isEmpty()) {
            throw new BizException("该项目尚未初始化上线检查清单");
        }

        int total = items.size();
        long passed = items.stream().filter(i -> i.getCheckResult() == CheckResult.PASS).count();
        long failed = items.stream().filter(i -> i.getCheckResult() == CheckResult.FAIL).count();
        long warning = items.stream().filter(i -> i.getCheckResult() == CheckResult.WARNING).count();
        long unchecked = items.stream().filter(i -> i.getCheckResult() == CheckResult.UNCHECKED).count();

        boolean ready = failed == 0 && unchecked == 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("total", total);
        result.put("passed", passed);
        result.put("failed", failed);
        result.put("warning", warning);
        result.put("unchecked", unchecked);
        result.put("ready", ready);
        result.put("message", ready ? "所有检查项已通过，允许上线" : "存在未通过或未检查的项目，不允许上线");
        return result;
    }
}
