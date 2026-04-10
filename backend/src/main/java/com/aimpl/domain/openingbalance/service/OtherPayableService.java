package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.openingbalance.dto.OtherPayableCreateDTO;
import com.aimpl.domain.openingbalance.entity.OtherPayable;
import com.aimpl.domain.openingbalance.mapper.OtherPayableMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherPayableService extends ServiceImpl<OtherPayableMapper, OtherPayable> {

    private final ProjectMapper projectMapper;

    @Transactional
    public OtherPayable create(OtherPayableCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        OtherPayable p = new OtherPayable();
        p.setProjectId(dto.getProjectId());
        p.setSubjectCode(dto.getSubjectCode());
        p.setObjectType(dto.getObjectType());
        p.setObjectId(dto.getObjectId());
        p.setObjectName(dto.getObjectName());
        p.setSummary(dto.getSummary());
        p.setAmount(dto.getAmount());
        p.setOccurDate(dto.getOccurDate());
        p.setRemark(dto.getRemark());
        save(p);
        return p;
    }

    public List<OtherPayable> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<OtherPayable>()
                .eq(OtherPayable::getProjectId, projectId));
    }
}
