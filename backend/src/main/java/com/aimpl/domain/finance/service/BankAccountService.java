package com.aimpl.domain.finance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.finance.dto.BankAccountCreateDTO;
import com.aimpl.domain.finance.entity.BankAccount;
import com.aimpl.domain.finance.mapper.BankAccountMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankAccountService extends ServiceImpl<BankAccountMapper, BankAccount> {

    @Transactional
    public BankAccount createBankAccount(BankAccountCreateDTO dto) {
        if (count(new LambdaQueryWrapper<BankAccount>()
                .eq(BankAccount::getAccountCode, dto.getAccountCode())) > 0) {
            throw new BizException("账户编码已存在: " + dto.getAccountCode());
        }

        BankAccount a = new BankAccount();
        a.setAccountCode(dto.getAccountCode());
        a.setAccountName(dto.getAccountName());
        a.setAccountType(dto.getAccountType());
        a.setBankName(dto.getBankName());
        a.setBankAccountNo(dto.getBankAccountNo());
        a.setBankBranch(dto.getBankBranch());
        a.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        a.setSubjectCode(dto.getSubjectCode());
        a.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(a);
        return a;
    }
}
