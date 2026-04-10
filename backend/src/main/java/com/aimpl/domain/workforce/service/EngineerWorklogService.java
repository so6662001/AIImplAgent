package com.aimpl.domain.workforce.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.dto.EngineerWorklogCreateDTO;
import com.aimpl.domain.workforce.entity.EngineerWorklog;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.mapper.EngineerWorklogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineerWorklogService extends ServiceImpl<EngineerWorklogMapper, EngineerWorklog> {

    private final EngineerMapper engineerMapper;
    private final ProjectMapper projectMapper;

    @Transactional
    public EngineerWorklog createWorklog(EngineerWorklogCreateDTO dto) {
        if (engineerMapper.selectById(dto.getEngineerId()) == null) {
            throw new BizException("工程师不存在: " + dto.getEngineerId());
        }
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        boolean exists = count(new LambdaQueryWrapper<EngineerWorklog>()
                .eq(EngineerWorklog::getEngineerId, dto.getEngineerId())
                .eq(EngineerWorklog::getWorkDate, dto.getWorkDate())) > 0;
        if (exists) {
            throw new BizException("该工程师当天已存在工作日志");
        }

        EngineerWorklog wl = new EngineerWorklog();
        wl.setEngineerId(dto.getEngineerId());
        wl.setProjectId(dto.getProjectId());
        wl.setWorkDate(dto.getWorkDate());
        wl.setTasksPlan(dto.getTasksPlan());
        wl.setTasksCompleted(dto.getTasksCompleted());
        wl.setDocumentsSubmitted(dto.getDocumentsSubmitted());
        wl.setIssues(dto.getIssues());
        wl.setNextDayPlan(dto.getNextDayPlan());
        wl.setStatus("DRAFT");
        save(wl);
        return wl;
    }

    @Transactional
    public EngineerWorklog submitWorklog(Long id) {
        EngineerWorklog wl = getById(id);
        if (wl == null) {
            throw new BizException("工作日志不存在: " + id);
        }
        if (!"DRAFT".equals(wl.getStatus())) {
            throw new BizException("只有草稿状态的日志才能提交");
        }
        wl.setStatus("SUBMITTED");
        updateById(wl);
        return wl;
    }

    public List<EngineerWorklog> listByQuery(Long engineerId, Long projectId) {
        LambdaQueryWrapper<EngineerWorklog> qw = new LambdaQueryWrapper<>();
        if (engineerId != null) {
            qw.eq(EngineerWorklog::getEngineerId, engineerId);
        }
        if (projectId != null) {
            qw.eq(EngineerWorklog::getProjectId, projectId);
        }
        qw.orderByDesc(EngineerWorklog::getWorkDate);
        return list(qw);
    }
}
