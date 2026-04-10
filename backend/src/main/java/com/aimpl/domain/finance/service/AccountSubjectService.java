package com.aimpl.domain.finance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.finance.dto.AccountSubjectCreateDTO;
import com.aimpl.domain.finance.entity.AccountSubject;
import com.aimpl.domain.finance.mapper.AccountSubjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountSubjectService extends ServiceImpl<AccountSubjectMapper, AccountSubject> {

    @Transactional
    public AccountSubject createSubject(AccountSubjectCreateDTO dto) {
        if (count(new LambdaQueryWrapper<AccountSubject>()
                .eq(AccountSubject::getSubjectCode, dto.getSubjectCode())) > 0) {
            throw new BizException("科目编码已存在: " + dto.getSubjectCode());
        }

        if (dto.getParentCode() != null && !dto.getParentCode().isBlank()) {
            if (count(new LambdaQueryWrapper<AccountSubject>()
                    .eq(AccountSubject::getSubjectCode, dto.getParentCode())) == 0) {
                throw new BizException("上级科目不存在: " + dto.getParentCode());
            }
        }

        AccountSubject s = new AccountSubject();
        s.setSubjectCode(dto.getSubjectCode());
        s.setSubjectName(dto.getSubjectName());
        s.setParentCode(dto.getParentCode());
        s.setSubjectCategory(dto.getSubjectCategory());
        s.setBalanceDirection(dto.getBalanceDirection());
        s.setAuxiliaryAccounting(dto.getAuxiliaryAccounting());
        s.setIsLeaf(dto.getIsLeaf() != null ? dto.getIsLeaf() : true);
        s.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        save(s);
        return s;
    }
}
