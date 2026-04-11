package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.mapper.CustomerMapper;
import com.aimpl.domain.archive.mapper.ProductMapper;
import com.aimpl.domain.archive.mapper.SupplierMapper;
import com.aimpl.domain.openingbalance.dto.InvoiceBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.InvoiceBalance;
import com.aimpl.domain.openingbalance.mapper.InvoiceBalanceMapper;
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
public class InvoiceBalanceService extends ServiceImpl<InvoiceBalanceMapper, InvoiceBalance> {

    private final ProjectMapper projectMapper;
    private final CustomerMapper customerMapper;
    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;

    @Transactional
    public InvoiceBalance create(InvoiceBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        if (productMapper.selectById(dto.getProductId()) == null) {
            throw new BizException("货品不存在: " + dto.getProductId());
        }

        if ("SALES_OUTPUT".equals(dto.getInvoiceType())) {
            if (customerMapper.selectById(dto.getCounterpartyId()) == null) {
                throw new BizException("客户不存在: " + dto.getCounterpartyId());
            }
        } else if ("PURCHASE_INPUT".equals(dto.getInvoiceType())) {
            if (supplierMapper.selectById(dto.getCounterpartyId()) == null) {
                throw new BizException("供应商不存在: " + dto.getCounterpartyId());
            }
        } else {
            throw new BizException("发票类型无效: " + dto.getInvoiceType());
        }

        BigDecimal taxAmount = dto.getAmount().multiply(dto.getTaxRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = dto.getAmount().add(taxAmount);

        InvoiceBalance b = new InvoiceBalance();
        b.setProjectId(dto.getProjectId());
        b.setInvoiceType(dto.getInvoiceType());
        b.setCounterpartyId(dto.getCounterpartyId());
        b.setCounterpartyName(dto.getCounterpartyName());
        b.setDocNo(dto.getDocNo());
        b.setProductId(dto.getProductId());
        b.setProductName(dto.getProductName());
        b.setSpec(dto.getSpec());
        b.setMaterial(dto.getMaterial());
        b.setQuantity(dto.getQuantity());
        b.setUnitPrice(dto.getUnitPrice());
        b.setAmount(dto.getAmount());
        b.setTaxRate(dto.getTaxRate());
        b.setTaxAmount(taxAmount);
        b.setTotalAmount(totalAmount);
        b.setDocDate(dto.getDocDate());
        b.setRemark(dto.getRemark());
        save(b);
        return b;
    }

    public List<InvoiceBalance> listByProjectAndType(Long projectId, String invoiceType) {
        LambdaQueryWrapper<InvoiceBalance> qw = new LambdaQueryWrapper<InvoiceBalance>()
                .eq(InvoiceBalance::getProjectId, projectId);
        if (invoiceType != null && !invoiceType.isBlank()) {
            qw.eq(InvoiceBalance::getInvoiceType, invoiceType);
        }
        return list(qw);
    }
}
