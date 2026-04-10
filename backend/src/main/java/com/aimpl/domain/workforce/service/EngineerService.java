package com.aimpl.domain.workforce.service;

import com.aimpl.common.enums.EngineerLevel;
import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EngineerService extends ServiceImpl<EngineerMapper, Engineer> {

    @Transactional
    public Engineer create(EngineerCreateDTO dto) {
        boolean codeExists = count(new LambdaQueryWrapper<Engineer>()
                .eq(Engineer::getEngineerCode, dto.getEngineerCode())) > 0;
        if (codeExists) {
            throw new BizException("工程师编号已存在: " + dto.getEngineerCode());
        }

        EngineerLevel level;
        try {
            level = EngineerLevel.valueOf(dto.getLevel());
        } catch (IllegalArgumentException e) {
            throw new BizException("无效的工程师级别: " + dto.getLevel());
        }

        Engineer entity = new Engineer();
        entity.setEngineerCode(dto.getEngineerCode());
        entity.setName(dto.getName());
        entity.setLevel(level);
        entity.setSkills(dto.getSkills());
        entity.setCurrentStatus(EngineerStatus.IDLE);
        entity.setJoinDate(dto.getJoinDate());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setMonthlyProjectCount(0);
        save(entity);
        return entity;
    }
}
