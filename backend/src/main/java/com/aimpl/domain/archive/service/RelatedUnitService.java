package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.dto.RelatedUnitCreateDTO;
import com.aimpl.domain.archive.entity.RelatedUnit;
import com.aimpl.domain.archive.mapper.RelatedUnitMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RelatedUnitService extends ServiceImpl<RelatedUnitMapper, RelatedUnit> {

    public RelatedUnit createRelatedUnit(RelatedUnitCreateDTO dto) {
        if (count(new LambdaQueryWrapper<RelatedUnit>()
                .eq(RelatedUnit::getUnitCode, dto.getUnitCode())) > 0) {
            throw new BizException("单位编码已存在: " + dto.getUnitCode());
        }
        if (dto.getFullName() != null && count(new LambdaQueryWrapper<RelatedUnit>()
                .eq(RelatedUnit::getFullName, dto.getFullName())) > 0) {
            throw new BizException("单位名称已存在: " + dto.getFullName());
        }

        RelatedUnit u = new RelatedUnit();
        u.setUnitCode(dto.getUnitCode());
        u.setFullName(dto.getFullName());
        u.setUnitType(dto.getUnitType());
        u.setCreditCode(dto.getCreditCode());
        u.setContactPerson(dto.getContactPerson());
        u.setPhone(dto.getPhone());
        u.setAddress(dto.getAddress());
        u.setBankName(dto.getBankName());
        u.setBankAccount(dto.getBankAccount());
        u.setEnabled(true);
        save(u);
        return u;
    }
}
