package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.mapper.SupplierMapper;
import com.aimpl.domain.openingbalance.dto.SupplierBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.SupplierBalance;
import com.aimpl.domain.openingbalance.mapper.SupplierBalanceMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierBalanceService extends ServiceImpl<SupplierBalanceMapper, SupplierBalance> {

    private final ProjectMapper projectMapper;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierBalance create(SupplierBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        if (supplierMapper.selectById(dto.getSupplierId()) == null) {
            throw new BizException("供应商不存在: " + dto.getSupplierId());
        }

        SupplierBalance b = new SupplierBalance();
        b.setProjectId(dto.getProjectId());
        b.setSupplierId(dto.getSupplierId());
        b.setDocType(dto.getDocType());
        b.setDocNo(dto.getDocNo());
        b.setDocDate(dto.getDocDate());
        b.setPayableAmount(dto.getPayableAmount());
        b.setPaidAmount(dto.getPaidAmount());
        b.setBalance(dto.getBalance());
        b.setExpectedDate(dto.getExpectedDate());
        b.setRemark(dto.getRemark());

        if (dto.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            b.setBalanceType("应付账款");
        } else if (dto.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            b.setBalanceType("预付账款");
        } else {
            b.setBalanceType("应付账款");
        }

        save(b);
        return b;
    }

    public List<SupplierBalance> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<SupplierBalance>()
                .eq(SupplierBalance::getProjectId, projectId));
    }
}
