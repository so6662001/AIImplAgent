package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.mapper.CustomerMapper;
import com.aimpl.domain.openingbalance.dto.CustomerBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.CustomerBalance;
import com.aimpl.domain.openingbalance.mapper.CustomerBalanceMapper;
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
public class CustomerBalanceService extends ServiceImpl<CustomerBalanceMapper, CustomerBalance> {

    private final ProjectMapper projectMapper;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerBalance create(CustomerBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }
        if (customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException("客户不存在: " + dto.getCustomerId());
        }

        CustomerBalance b = new CustomerBalance();
        b.setProjectId(dto.getProjectId());
        b.setCustomerId(dto.getCustomerId());
        b.setDocType(dto.getDocType());
        b.setDocNo(dto.getDocNo());
        b.setDocDate(dto.getDocDate());
        b.setReceivableAmount(dto.getReceivableAmount());
        b.setReceivedAmount(dto.getReceivedAmount());
        b.setBalance(dto.getBalance());
        b.setExpectedDate(dto.getExpectedDate());
        b.setRemark(dto.getRemark());

        if (dto.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            b.setBalanceType("应收账款");
        } else if (dto.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            b.setBalanceType("预收账款");
        } else {
            b.setBalanceType("应收账款");
        }

        save(b);
        return b;
    }

    public List<CustomerBalance> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<CustomerBalance>()
                .eq(CustomerBalance::getProjectId, projectId));
    }
}
