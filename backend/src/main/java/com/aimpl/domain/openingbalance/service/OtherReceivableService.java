package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.openingbalance.dto.OtherReceivableCreateDTO;
import com.aimpl.domain.openingbalance.entity.OtherReceivable;
import com.aimpl.domain.openingbalance.mapper.OtherReceivableMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OtherReceivableService extends ServiceImpl<OtherReceivableMapper, OtherReceivable> {

    private final ProjectMapper projectMapper;

    @Transactional
    public OtherReceivable create(OtherReceivableCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        OtherReceivable r = new OtherReceivable();
        r.setProjectId(dto.getProjectId());
        r.setSubjectCode(dto.getSubjectCode());
        r.setObjectType(dto.getObjectType());
        r.setObjectId(dto.getObjectId());
        r.setObjectName(dto.getObjectName());
        r.setSummary(dto.getSummary());
        r.setAmount(dto.getAmount());
        r.setOccurDate(dto.getOccurDate());
        r.setRemark(dto.getRemark());
        save(r);
        return r;
    }

    public List<OtherReceivable> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<OtherReceivable>()
                .eq(OtherReceivable::getProjectId, projectId));
    }
}
