package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.dto.StorageLocationCreateDTO;
import com.aimpl.domain.archive.entity.StorageLocation;
import com.aimpl.domain.archive.entity.Warehouse;
import com.aimpl.domain.archive.mapper.StorageLocationMapper;
import com.aimpl.domain.archive.mapper.WarehouseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageLocationService extends ServiceImpl<StorageLocationMapper, StorageLocation> {

    private final WarehouseMapper warehouseMapper;

    @Transactional
    public StorageLocation createLocation(StorageLocationCreateDTO dto) {
        if (count(new LambdaQueryWrapper<StorageLocation>()
                .eq(StorageLocation::getLocationCode, dto.getLocationCode())) > 0) {
            throw new BizException("库位编码已存在: " + dto.getLocationCode());
        }

        if (warehouseMapper.selectById(dto.getWarehouseId()) == null) {
            throw new BizException("仓库不存在: " + dto.getWarehouseId());
        }

        StorageLocation loc = new StorageLocation();
        loc.setLocationCode(dto.getLocationCode());
        loc.setLocationName(dto.getLocationName());
        loc.setWarehouseId(dto.getWarehouseId());
        loc.setLocationType(dto.getLocationType());
        loc.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(loc);
        return loc;
    }

    public List<StorageLocation> listByWarehouseId(Long warehouseId) {
        return list(new LambdaQueryWrapper<StorageLocation>()
                .eq(StorageLocation::getWarehouseId, warehouseId));
    }
}
