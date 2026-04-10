package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.finance.entity.BankAccount;
import com.aimpl.domain.finance.mapper.BankAccountMapper;
import com.aimpl.domain.openingbalance.dto.AccountBalanceCreateDTO;
import com.aimpl.domain.openingbalance.entity.AccountBalance;
import com.aimpl.domain.openingbalance.mapper.AccountBalanceMapper;
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
public class AccountBalanceService extends ServiceImpl<AccountBalanceMapper, AccountBalance> {

    private final ProjectMapper projectMapper;
    private final BankAccountMapper bankAccountMapper;

    @Transactional
    public AccountBalance create(AccountBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        BankAccount account = bankAccountMapper.selectById(dto.getBankAccountId());
        if (account == null) {
            throw new BizException("银行账户不存在: " + dto.getBankAccountId());
        }

        if ("现金".equals(account.getAccountType())
                && dto.getOpeningBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("现金账户期初余额不能为负数");
        }

        AccountBalance b = new AccountBalance();
        b.setProjectId(dto.getProjectId());
        b.setBankAccountId(dto.getBankAccountId());
        b.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : account.getCurrency());
        b.setOpeningBalance(dto.getOpeningBalance());
        b.setRemark(dto.getRemark());
        save(b);
        return b;
    }

    public List<AccountBalance> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<AccountBalance>()
                .eq(AccountBalance::getProjectId, projectId));
    }
}
