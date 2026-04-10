package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.mapper.ProductMapper;
import com.aimpl.domain.archive.mapper.StorageLocationMapper;
import com.aimpl.domain.archive.mapper.WarehouseMapper;
import com.aimpl.domain.openingbalance.dto.InventoryBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.InventoryBalance;
import com.aimpl.domain.openingbalance.mapper.InventoryBalanceMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryBalanceService extends ServiceImpl<InventoryBalanceMapper, InventoryBalance> {

    private final ProjectMapper projectMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final StorageLocationMapper storageLocationMapper;

    @Transactional
    public InventoryBalance create(InventoryBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        if (productMapper.selectById(dto.getProductId()) == null) {
            throw new BizException("货品不存在: " + dto.getProductId());
        }
        if (warehouseMapper.selectById(dto.getWarehouseId()) == null) {
            throw new BizException("仓库不存在: " + dto.getWarehouseId());
        }
        if (dto.getLocationId() != null && storageLocationMapper.selectById(dto.getLocationId()) == null) {
            throw new BizException("库位不存在: " + dto.getLocationId());
        }

        InventoryBalance b = new InventoryBalance();
        b.setProjectId(dto.getProjectId());
        b.setProductId(dto.getProductId());
        b.setWarehouseId(dto.getWarehouseId());
        b.setLocationId(dto.getLocationId());
        b.setBatchNo(dto.getBatchNo());
        b.setInboundDate(dto.getInboundDate());
        b.setQuantity(dto.getQuantity());
        b.setWeight(dto.getWeight());
        b.setPackQuantity(dto.getPackQuantity());
        b.setCostUnitPrice(dto.getCostUnitPrice());

        if (dto.getPackQuantity() != null && dto.getPackQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal wholeUnits = dto.getQuantity().divideToIntegralValue(dto.getPackQuantity());
            b.setWholeUnits(wholeUnits);
            b.setOddUnits(dto.getQuantity().subtract(wholeUnits.multiply(dto.getPackQuantity())));
        } else {
            b.setWholeUnits(BigDecimal.ZERO);
            b.setOddUnits(dto.getQuantity());
        }

        if (dto.getWeight() != null && dto.getCostUnitPrice() != null) {
            b.setCostAmount(dto.getWeight().multiply(dto.getCostUnitPrice())
                    .setScale(2, RoundingMode.HALF_UP));
        }

        if (dto.getQuantity() != null && dto.getQuantity().compareTo(BigDecimal.ZERO) > 0
                && dto.getWeight() != null) {
            b.setUnitWeight(dto.getWeight().multiply(new BigDecimal("1000"))
                    .divide(dto.getQuantity(), 3, RoundingMode.HALF_UP));
        } else {
            b.setUnitWeight(null);
        }

        save(b);
        return b;
    }

    public List<InventoryBalance> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<InventoryBalance>()
                .eq(InventoryBalance::getProjectId, projectId));
    }
}
