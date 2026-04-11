package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.TrainingDashboardDTO;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.aimpl.domain.training.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingDashboardService {

    private final ProjectMapper projectMapper;
    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final RequiredCourseMapper requiredCourseMapper;
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
            int totalFlags = logs.size() * 6;
            int completed = 0;
            for (TrainingDailyLog log : logs) {
                if (Boolean.TRUE.equals(log.getPlanUploaded())) completed++;
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

    public EnhancedDashboardVO getEnhancedDashboard(Long projectId) {
        TrainingDashboardDTO basicDashboard = getDashboard(projectId);

        Project project = projectMapper.selectById(projectId);
        String industryType = project.getIndustryType() != null
                ? project.getIndustryType().name() : null;

        List<String> allModules = industryType != null
                ? requiredCourseMapper.selectList(
                        new LambdaQueryWrapper<RequiredCourse>()
                                .eq(RequiredCourse::getIndustryType, industryType))
                        .stream().map(RequiredCourse::getCourseModule)
                        .distinct().collect(Collectors.toList())
                : List.of();

        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        Map<Long, List<ExamRecord>> examsByTrainee = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getTraineeId));
        Map<String, List<ExamRecord>> examsByModule = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getModule));

        List<TrainingDailyLog> logs = trainingDailyLogMapper.selectList(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId)
                        .orderByAsc(TrainingDailyLog::getLogDate));

        int totalModuleCount = Math.max(allModules.size(), 1);

        List<TraineeStatusVO> traineeStatuses = new ArrayList<>();
        for (TraineeProfile t : trainees) {
            TraineeStatusVO ts = new TraineeStatusVO();
            ts.setId(t.getId());
            ts.setName(t.getEmployeeName());
            ts.setRole(t.getRole());
            ts.setKaUser(Boolean.TRUE.equals(t.getKaUser()));
            ts.setProgressPercent(t.getProgressPercent() != null ? t.getProgressPercent() : 0);
            ts.setAttendanceDays(t.getAttendanceDays() != null ? t.getAttendanceDays() : 0);
            ts.setTotalDays(t.getTotalDays() != null ? t.getTotalDays() : 0);
            ts.setRiskLevel(t.getRiskLevel() != null ? t.getRiskLevel() : "NORMAL");

            List<ExamRecord> traineeExams = examsByTrainee.getOrDefault(t.getId(), List.of());
            long passedModules = allModules.stream()
                    .filter(m -> traineeExams.stream()
                            .anyMatch(e -> e.getModule().equals(m) && e.getScore() >= 70))
                    .count();
            ts.setPassedModules((int) passedModules);
            ts.setTotalModules(allModules.size());
            traineeStatuses.add(ts);
        }

        Set<String> trainedModules = logs.stream()
                .map(TrainingDailyLog::getTopic)
                .collect(Collectors.toSet());

        Set<String> allModuleSet = new LinkedHashSet<>(allModules);
        allModuleSet.addAll(examsByModule.keySet());

        List<ModuleTrainingStatusVO> moduleStatuses = new ArrayList<>();
        for (String module : allModuleSet) {
            ModuleTrainingStatusVO ms = new ModuleTrainingStatusVO();
            ms.setModule(module);
            ms.setTrained(trainedModules.contains(module));
            List<ExamRecord> moduleExams = examsByModule.getOrDefault(module, List.of());
            ms.setExamCount(moduleExams.size());
            if (moduleExams.isEmpty()) {
                ms.setPassRate(BigDecimal.ZERO);
            } else {
                long passed = moduleExams.stream().filter(ExamRecord::getPassed).count();
                ms.setPassRate(BigDecimal.valueOf(passed * 100.0 / moduleExams.size())
                        .setScale(2, RoundingMode.HALF_UP));
            }

            String topWeak = moduleExams.stream()
                    .filter(e -> e.getWeakPoints() != null && !e.getWeakPoints().isBlank())
                    .collect(Collectors.groupingBy(ExamRecord::getWeakPoints, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            ms.setTopWeakPoint(topWeak);
            moduleStatuses.add(ms);
        }

        List<DayDocumentStatusVO> documentMatrix = new ArrayList<>();
        for (TrainingDailyLog log : logs) {
            DayDocumentStatusVO ds = new DayDocumentStatusVO();
            ds.setDate(log.getLogDate());
            ds.setPlanUploaded(Boolean.TRUE.equals(log.getPlanUploaded()));
            ds.setCoursewareUploaded(Boolean.TRUE.equals(log.getCoursewareUploaded()));
            ds.setSignInCompleted(Boolean.TRUE.equals(log.getSignInCompleted()));
            ds.setSummaryUploaded(Boolean.TRUE.equals(log.getSummaryUploaded()));
            ds.setExamConducted(Boolean.TRUE.equals(log.getExamConducted()));
            ds.setDailyReportSubmitted(Boolean.TRUE.equals(log.getDailyReportSubmitted()));
            int count = 0;
            if (ds.isPlanUploaded()) count++;
            if (ds.isCoursewareUploaded()) count++;
            if (ds.isSignInCompleted()) count++;
            if (ds.isSummaryUploaded()) count++;
            if (ds.isExamConducted()) count++;
            if (ds.isDailyReportSubmitted()) count++;
            ds.setCompletionRate(BigDecimal.valueOf(count * 100.0 / 6)
                    .setScale(2, RoundingMode.HALF_UP));
            documentMatrix.add(ds);
        }

        List<String> riskWarnings = new ArrayList<>();

        for (TraineeStatusVO ts : traineeStatuses) {
            if (ts.isKaUser() && ts.getProgressPercent() < 80) {
                riskWarnings.add("学员" + ts.getName() + "为KA用户但进度仅" + ts.getProgressPercent() + "%，风险较高");
            }
        }

        for (ModuleTrainingStatusVO ms : moduleStatuses) {
            if (ms.getExamCount() > 0 && ms.getPassRate().compareTo(BigDecimal.valueOf(80)) < 0) {
                riskWarnings.add("模块" + ms.getModule() + "考核通过率仅" + ms.getPassRate() + "%，低于合格线80%");
            }
        }

        for (TrainingDailyLog log : logs) {
            if (!Boolean.TRUE.equals(log.getSummaryUploaded())) {
                riskWarnings.add(log.getLogDate() + "的培训纪要尚未提交");
            }
        }

        int totalDays = logs.size();
        if (totalDays > 0) {
            for (TraineeStatusVO ts : traineeStatuses) {
                double attendanceRate = ts.getTotalDays() > 0
                        ? ts.getAttendanceDays() * 100.0 / ts.getTotalDays() : 100.0;
                if (attendanceRate < 90) {
                    riskWarnings.add("学员" + ts.getName() + "出勤率低于90%");
                }
            }
        }

        EnhancedDashboardVO vo = new EnhancedDashboardVO();
        vo.setBasicDashboard(basicDashboard);
        vo.setTraineeStatuses(traineeStatuses);
        vo.setModuleStatuses(moduleStatuses);
        vo.setDocumentMatrix(documentMatrix);
        vo.setRiskWarnings(riskWarnings);
        return vo;
    }
}
