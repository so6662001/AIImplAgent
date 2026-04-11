package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.TraineeProfileCreateDTO;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TraineeProfileService extends ServiceImpl<TraineeProfileMapper, TraineeProfile> {

    private final ProjectMapper projectMapper;

    @Transactional
    public TraineeProfile createTrainee(TraineeProfileCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        boolean nameExists = count(new LambdaQueryWrapper<TraineeProfile>()
                .eq(TraineeProfile::getProjectId, dto.getProjectId())
                .eq(TraineeProfile::getEmployeeName, dto.getEmployeeName())) > 0;
        if (nameExists) {
            throw new BizException("该项目下员工姓名已存在: " + dto.getEmployeeName());
        }

        TraineeProfile tp = new TraineeProfile();
        tp.setProjectId(dto.getProjectId());
        tp.setEmployeeName(dto.getEmployeeName());
        tp.setRole(dto.getRole());
        tp.setDepartment(dto.getDepartment());
        tp.setKaUser(dto.getKaUser() != null && dto.getKaUser());
        tp.setProgressPercent(0);
        tp.setAttendanceDays(0);
        tp.setTotalDays(0);
        tp.setRiskLevel("NORMAL");
        save(tp);
        return tp;
    }

    public List<TraineeProfile> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<TraineeProfile>()
                .eq(TraineeProfile::getProjectId, projectId));
    }
}
