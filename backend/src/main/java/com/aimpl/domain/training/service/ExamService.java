package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
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

@Service
@RequiredArgsConstructor
public class ExamService extends ServiceImpl<ExamRecordMapper, ExamRecord> {

    private static final int PASS_SCORE = 70;
    private static final int KA_COMPREHENSIVE_PASS = 75;

    private final TraineeProfileMapper traineeMapper;

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
     * 检查指定项目的所有KA用户是否全部通过必学课程。
     * 返回 true 表示允许推进上线。
     */
    public boolean checkGoLiveReadiness(Long projectId) {
        List<TraineeProfile> kaUsers = traineeMapper.selectList(
                new LambdaQueryWrapper<TraineeProfile>()
                        .eq(TraineeProfile::getProjectId, projectId)
                        .eq(TraineeProfile::getKaUser, true));

        if (kaUsers.isEmpty()) {
            throw new BizException("该项目未设置KA用户，请先标记关键用户");
        }

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
