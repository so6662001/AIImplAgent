package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.TrainingDashboardDTO;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingDashboardService {

    private final ProjectMapper projectMapper;
    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final ExamService examService;

    public TrainingDashboardDTO getDashboard(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        TrainingDashboardDTO dto = new TrainingDashboardDTO();
        dto.setProjectId(projectId);

        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));
        dto.setTotalTrainees(trainees.size());
        dto.setKaUserCount((int) trainees.stream()
                .filter(t -> Boolean.TRUE.equals(t.getKaUser())).count());

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));

        if (exams.isEmpty()) {
            dto.setOverallPassRate(BigDecimal.ZERO);
        } else {
            long passedCount = exams.stream().filter(ExamRecord::getPassed).count();
            dto.setOverallPassRate(BigDecimal.valueOf(passedCount * 100.0 / exams.size())
                    .setScale(2, RoundingMode.HALF_UP));
        }

        Map<String, List<ExamRecord>> byModule = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getModule));
        List<TrainingDashboardDTO.ModuleStat> moduleStats = new ArrayList<>();
        for (Map.Entry<String, List<ExamRecord>> entry : byModule.entrySet()) {
            List<ExamRecord> moduleExams = entry.getValue();
            long passed = moduleExams.stream().filter(ExamRecord::getPassed).count();
            BigDecimal rate = BigDecimal.valueOf(passed * 100.0 / moduleExams.size())
                    .setScale(2, RoundingMode.HALF_UP);
            moduleStats.add(new TrainingDashboardDTO.ModuleStat(entry.getKey(), moduleExams.size(), rate));
        }
        dto.setModuleStats(moduleStats);

        if (trainees.isEmpty()) {
            dto.setAttendanceRate(BigDecimal.ZERO);
        } else {
            int totalAttendance = trainees.stream()
                    .mapToInt(t -> t.getAttendanceDays() != null ? t.getAttendanceDays() : 0).sum();
            int totalDays = trainees.stream()
                    .mapToInt(t -> t.getTotalDays() != null && t.getTotalDays() > 0 ? t.getTotalDays() : 1).sum();
            dto.setAttendanceRate(BigDecimal.valueOf(totalAttendance * 100.0 / totalDays)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        List<TrainingDailyLog> logs = trainingDailyLogMapper.selectList(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId));
        if (logs.isEmpty()) {
            dto.setDocumentCompletionRate(BigDecimal.ZERO);
        } else {
            int totalFlags = logs.size() * 5;
            int completed = 0;
            for (TrainingDailyLog log : logs) {
                if (Boolean.TRUE.equals(log.getSignInCompleted())) completed++;
                if (Boolean.TRUE.equals(log.getCoursewareUploaded())) completed++;
                if (Boolean.TRUE.equals(log.getSummaryUploaded())) completed++;
                if (Boolean.TRUE.equals(log.getExamConducted())) completed++;
                if (Boolean.TRUE.equals(log.getDailyReportSubmitted())) completed++;
            }
            dto.setDocumentCompletionRate(BigDecimal.valueOf(completed * 100.0 / totalFlags)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        try {
            dto.setGoLiveReady(examService.checkGoLiveReadiness(projectId));
        } catch (BizException e) {
            dto.setGoLiveReady(false);
        }

        return dto;
    }
}
