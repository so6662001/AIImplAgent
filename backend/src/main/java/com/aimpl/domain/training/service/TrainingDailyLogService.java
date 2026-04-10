package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.TrainingDailyLogCreateDTO;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingDailyLogService extends ServiceImpl<TrainingDailyLogMapper, TrainingDailyLog> {

    private final ProjectMapper projectMapper;

    @Transactional
    public TrainingDailyLog createLog(TrainingDailyLogCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        boolean dateExists = count(new LambdaQueryWrapper<TrainingDailyLog>()
                .eq(TrainingDailyLog::getProjectId, dto.getProjectId())
                .eq(TrainingDailyLog::getLogDate, dto.getLogDate())) > 0;
        if (dateExists) {
            throw new BizException("该项目当天已存在培训日志");
        }

        TrainingDailyLog log = new TrainingDailyLog();
        log.setProjectId(dto.getProjectId());
        log.setLogDate(dto.getLogDate());
        log.setTopic(dto.getTopic());
        log.setTrainerName(dto.getTrainerName());
        log.setAttendeeCount(dto.getAttendeeCount());
        log.setSignInCompleted(dto.getSignInCompleted() != null && dto.getSignInCompleted());
        log.setCoursewareUploaded(dto.getCoursewareUploaded() != null && dto.getCoursewareUploaded());
        log.setSummaryUploaded(dto.getSummaryUploaded() != null && dto.getSummaryUploaded());
        log.setExamConducted(dto.getExamConducted() != null && dto.getExamConducted());
        log.setDailyReportSubmitted(dto.getDailyReportSubmitted() != null && dto.getDailyReportSubmitted());
        log.setIssues(dto.getIssues());
        save(log);
        return log;
    }

    @Transactional
    public TrainingDailyLog updateLog(Long id, TrainingDailyLog update) {
        TrainingDailyLog existing = getById(id);
        if (existing == null) {
            throw new BizException("培训日志不存在: " + id);
        }
        if (update.getSignInCompleted() != null) existing.setSignInCompleted(update.getSignInCompleted());
        if (update.getCoursewareUploaded() != null) existing.setCoursewareUploaded(update.getCoursewareUploaded());
        if (update.getSummaryUploaded() != null) existing.setSummaryUploaded(update.getSummaryUploaded());
        if (update.getExamConducted() != null) existing.setExamConducted(update.getExamConducted());
        if (update.getDailyReportSubmitted() != null) existing.setDailyReportSubmitted(update.getDailyReportSubmitted());
        if (update.getIssues() != null) existing.setIssues(update.getIssues());
        updateById(existing);
        return existing;
    }

    public List<TrainingDailyLog> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<TrainingDailyLog>()
                .eq(TrainingDailyLog::getProjectId, projectId)
                .orderByAsc(TrainingDailyLog::getLogDate));
    }
}
