package com.aimpl.domain.training.service;

import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TraineeProgressService {

    private static final int PASS_SCORE = 70;

    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final RequiredCourseMapper requiredCourseMapper;
    private final ProjectMapper projectMapper;

    @Transactional
    public void recalculateProgress(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return;
        }

        List<TraineeProfile> trainees = traineeProfileMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId));
        if (trainees.isEmpty()) {
            return;
        }

        List<TrainingDailyLog> logs = trainingDailyLogMapper.selectList(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId));
        int totalDays = logs.size();
        int attendanceDays = (int) logs.stream()
                .filter(l -> Boolean.TRUE.equals(l.getSignInCompleted()))
                .count();

        String industryType = project.getIndustryType() != null
                ? project.getIndustryType().name() : null;

        List<RequiredCourse> allCourses = industryType != null
                ? requiredCourseMapper.selectList(
                        new LambdaQueryWrapper<RequiredCourse>()
                                .eq(RequiredCourse::getIndustryType, industryType))
                : List.of();

        List<ExamRecord> allExams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId));
        Map<Long, List<ExamRecord>> examsByTrainee = allExams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getTraineeId));

        for (TraineeProfile trainee : trainees) {
            boolean isKa = Boolean.TRUE.equals(trainee.getKaUser());

            List<String> requiredModules;
            if (isKa) {
                requiredModules = allCourses.stream()
                        .filter(c -> Boolean.TRUE.equals(c.getKaRequired()))
                        .map(RequiredCourse::getCourseModule)
                        .collect(Collectors.toList());
            } else {
                requiredModules = allCourses.stream()
                        .map(RequiredCourse::getCourseModule)
                        .collect(Collectors.toList());
            }

            int totalModules = Math.max(requiredModules.size(), 1);

            List<ExamRecord> traineeExams = examsByTrainee.getOrDefault(trainee.getId(), List.of());
            Map<String, Integer> bestScoreByModule = traineeExams.stream()
                    .filter(e -> requiredModules.contains(e.getModule()))
                    .collect(Collectors.toMap(
                            ExamRecord::getModule,
                            ExamRecord::getScore,
                            Math::max));

            long passedModules = bestScoreByModule.values().stream()
                    .filter(score -> score >= PASS_SCORE)
                    .count();

            int progressPercent = requiredModules.isEmpty()
                    ? 0
                    : (int) (passedModules * 100 / totalModules);

            String riskLevel;
            if (isKa && progressPercent < 50) {
                riskLevel = "HIGH";
            } else if (isKa && progressPercent < 80) {
                riskLevel = "MEDIUM";
            } else if (!isKa && progressPercent < 30) {
                riskLevel = "MEDIUM";
            } else {
                riskLevel = "NORMAL";
            }

            trainee.setProgressPercent(progressPercent);
            trainee.setAttendanceDays(attendanceDays);
            trainee.setTotalDays(totalDays);
            trainee.setRiskLevel(riskLevel);
            traineeProfileMapper.updateById(trainee);
        }
    }
}
