package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.dto.WarehouseCreateDTO;
import com.aimpl.domain.archive.entity.Warehouse;
import com.aimpl.domain.archive.mapper.WarehouseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService extends ServiceImpl<WarehouseMapper, Warehouse> {

    public Warehouse createWarehouse(WarehouseCreateDTO dto) {
        if (count(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, dto.getWarehouseCode())) > 0) {
            throw new BizException("仓库编码已存在: " + dto.getWarehouseCode());
        }

        Warehouse w = new Warehouse();
        w.setWarehouseCode(dto.getWarehouseCode());
        w.setWarehouseName(dto.getWarehouseName());
        w.setWarehouseType(dto.getWarehouseType());
        w.setWarehouseNature(dto.getWarehouseNature());
        w.setManagementMode(dto.getManagementMode());
        w.setAddress(dto.getAddress());
        w.setContact(dto.getContact());
        w.setPhone(dto.getPhone());
        w.setAreaSqm(dto.getAreaSqm());
        w.setCraneCount(dto.getCraneCount());
        w.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(w);
        return w;
    }
}
