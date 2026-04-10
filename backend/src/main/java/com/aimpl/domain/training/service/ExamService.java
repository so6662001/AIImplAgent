package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService extends ServiceImpl<ExamRecordMapper, ExamRecord> {

    private static final int PASS_SCORE = 70;
    private static final int KA_COMPREHENSIVE_PASS = 75;

    private final TraineeProfileMapper traineeMapper;
    private final ProjectMapper projectMapper;
    private final RequiredCourseService requiredCourseService;

    @Transactional
    public ExamRecord submitExam(ExamRecordCreateDTO dto) {
        TraineeProfile trainee = traineeMapper.selectById(dto.getTraineeId());
        if (trainee == null) {
            throw new BizException("学员不存在: " + dto.getTraineeId());
        }
        if (!trainee.getProjectId().equals(dto.getProjectId())) {
            throw new BizException("学员不属于该项目");
        }

        ExamRecord record = new ExamRecord();
        record.setProjectId(dto.getProjectId());
        record.setTraineeId(dto.getTraineeId());
        record.setModule(dto.getModule());
        record.setExamType(dto.getExamType());
        record.setScore(dto.getScore());
        record.setPassed(dto.getScore() >= PASS_SCORE);
        record.setRequiredCourse(dto.getRequiredCourse() != null && dto.getRequiredCourse());
        record.setWeakPoints(dto.getWeakPoints());
        record.setRetryOf(dto.getRetryOf());
        record.setExamTime(LocalDateTime.now());
        save(record);
        return record;
    }

    /**
     * Check if all KA users in a project have passed ALL required courses
     * for the project's industry type, and their average score meets the
     * KA_COMPREHENSIVE_PASS threshold (75).
     *
     * Falls back to legacy behavior (checking requiredCourse flag) when
     * no RequiredCourse matrix is configured for the industry type.
     */
    public boolean checkGoLiveReadiness(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        List<TraineeProfile> kaUsers = traineeMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId)
                        .eq(TraineeProfile::getKaUser, true));

        if (kaUsers.isEmpty()) {
            throw new BizException("该项目未设置KA用户，请先标记关键用户");
        }

        List<String> requiredModules = null;
        if (project.getIndustryType() != null) {
            requiredModules = requiredCourseService
                    .getRequiredCourseModules(project.getIndustryType().name());
        }

        if (requiredModules != null && !requiredModules.isEmpty()) {
            return checkWithRequiredCourseMatrix(projectId, kaUsers, requiredModules);
        }
        return checkLegacy(projectId, kaUsers);
    }

    private boolean checkWithRequiredCourseMatrix(Long projectId,
                                                   List<TraineeProfile> kaUsers,
                                                   List<String> requiredModules) {
        for (TraineeProfile ka : kaUsers) {
            List<ExamRecord> allExams = list(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getProjectId, projectId)
                    .eq(ExamRecord::getTraineeId, ka.getId()));

            Map<String, Integer> bestScoreByModule = allExams.stream()
                    .filter(e -> requiredModules.contains(e.getModule()))
                    .collect(Collectors.toMap(
                            ExamRecord::getModule,
                            ExamRecord::getScore,
                            Math::max));

            for (String module : requiredModules) {
                Integer bestScore = bestScoreByModule.get(module);
                if (bestScore == null || bestScore < PASS_SCORE) {
                    return false;
                }
            }

            double avgScore = bestScoreByModule.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);
            if (avgScore < KA_COMPREHENSIVE_PASS) {
                return false;
            }
        }
        return true;
    }

    private boolean checkLegacy(Long projectId, List<TraineeProfile> kaUsers) {
        for (TraineeProfile ka : kaUsers) {
            List<ExamRecord> requiredExams = list(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getProjectId, projectId)
                    .eq(ExamRecord::getTraineeId, ka.getId())
                    .eq(ExamRecord::getRequiredCourse, true));

            if (requiredExams.isEmpty()) {
                return false;
            }

            boolean allPassed = requiredExams.stream().allMatch(ExamRecord::getPassed);
            if (!allPassed) {
                return false;
            }
        }
        return true;
    }
}
