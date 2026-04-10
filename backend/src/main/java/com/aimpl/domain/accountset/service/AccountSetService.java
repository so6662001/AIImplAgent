package com.aimpl.domain.accountset.service;

import com.aimpl.common.enums.AccountSetStatus;
import com.aimpl.common.exception.BizException;
import com.aimpl.domain.accountset.dto.AccountSetCreateDTO;
import com.aimpl.domain.accountset.entity.AccountSet;
import com.aimpl.domain.accountset.mapper.AccountSetMapper;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountSetService extends ServiceImpl<AccountSetMapper, AccountSet> {

    private final ProjectMapper projectMapper;

    @Transactional
    public AccountSet create(AccountSetCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        boolean exists = count(new LambdaQueryWrapper<AccountSet>()
                .eq(AccountSet::getProjectId, dto.getProjectId())) > 0;
        if (exists) {
            throw new BizException("该项目已存在帐套，每个项目仅允许一个帐套");
        }

        AccountSet entity = new AccountSet();
        entity.setProjectId(dto.getProjectId());
        entity.setSetName(dto.getSetName());
        entity.setAccountingSystem(dto.getAccountingSystem());
        entity.setPricingMethod(dto.getPricingMethod());
        entity.setQtyDecimals(dto.getQtyDecimals() != null ? dto.getQtyDecimals() : 2);
        entity.setWgtDecimals(dto.getWgtDecimals() != null ? dto.getWgtDecimals() : 2);
        entity.setPrcDecimals(dto.getPrcDecimals() != null ? dto.getPrcDecimals() : 2);
        entity.setAmtDecimals(dto.getAmtDecimals() != null ? dto.getAmtDecimals() : 2);
        entity.setUseWeight(dto.getUseWeight() != null ? dto.getUseWeight() : true);
        entity.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        entity.setFiscalYearStart(dto.getFiscalYearStart() != null ? dto.getFiscalYearStart() : 1);
        entity.setStatus(AccountSetStatus.DRAFT);
        save(entity);
        return entity;
    }

    public List<AccountSet> listByProjectId(Long projectId) {
        return list(new LambdaQueryWrapper<AccountSet>()
                .eq(AccountSet::getProjectId, projectId));
    }

    @Transactional
    public AccountSet activate(Long id) {
        AccountSet accountSet = getById(id);
        if (accountSet == null) {
            throw new BizException("帐套不存在: " + id);
        }
        if (accountSet.getStatus() != AccountSetStatus.DRAFT) {
            throw new BizException("只有草稿状态的帐套才能启用，当前状态: " + accountSet.getStatus().getLabel());
        }
        accountSet.setStatus(AccountSetStatus.ACTIVE);
        updateById(accountSet);
        return accountSet;
    }
}
