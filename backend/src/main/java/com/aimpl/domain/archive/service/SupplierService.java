package com.aimpl.domain.archive.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.dto.SupplierCreateDTO;
import com.aimpl.domain.archive.entity.Supplier;
import com.aimpl.domain.archive.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SupplierService extends ServiceImpl<SupplierMapper, Supplier> {

    public Supplier createSupplier(SupplierCreateDTO dto) {
        if (count(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getSupplierCode, dto.getSupplierCode())) > 0) {
            throw new BizException("供应商编码已存在: " + dto.getSupplierCode());
        }
        if (dto.getFullName() != null && count(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getFullName, dto.getFullName())) > 0) {
            throw new BizException("供应商名称已存在: " + dto.getFullName());
        }

        Supplier s = new Supplier();
        s.setSupplierCode(dto.getSupplierCode());
        s.setFullName(dto.getFullName());
        s.setShortName(dto.getShortName());
        s.setSupplierType(dto.getSupplierType());
        s.setCreditCode(dto.getCreditCode());
        s.setContact(dto.getContact());
        s.setPhone(dto.getPhone());
        s.setAddress(dto.getAddress());
        s.setSettlementMethod(dto.getSettlementMethod());
        s.setTaxRate(dto.getTaxRate());
        s.setEnabled(true);
        save(s);
        return s;
    }
}
