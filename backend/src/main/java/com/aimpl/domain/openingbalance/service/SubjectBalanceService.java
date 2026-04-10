package com.aimpl.domain.openingbalance.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.finance.entity.AccountSubject;
import com.aimpl.domain.finance.mapper.AccountSubjectMapper;
import com.aimpl.domain.openingbalance.dto.SubjectBalanceCreateDTO;
import com.aimpl.domain.openingbalance.dto.TrialBalanceDTO;
import com.aimpl.domain.openingbalance.entity.SubjectBalance;
import com.aimpl.domain.openingbalance.mapper.SubjectBalanceMapper;
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
public class SubjectBalanceService extends ServiceImpl<SubjectBalanceMapper, SubjectBalance> {

    private final ProjectMapper projectMapper;
    private final AccountSubjectMapper accountSubjectMapper;

    @Transactional
    public SubjectBalance create(SubjectBalanceCreateDTO dto) {
        if (projectMapper.selectById(dto.getProjectId()) == null) {
            throw new BizException("项目不存在: " + dto.getProjectId());
        }

        AccountSubject subject = accountSubjectMapper.selectOne(
                new LambdaQueryWrapper<AccountSubject>()
                        .eq(AccountSubject::getSubjectCode, dto.getSubjectCode()));
        if (subject == null) {
            throw new BizException("科目不存在: " + dto.getSubjectCode());
        }
        if (!Boolean.TRUE.equals(subject.getIsLeaf())) {
            throw new BizException("只能对末级科目录入余额: " + dto.getSubjectCode());
        }

        boolean debitPositive = dto.getDebitBalance() != null
                && dto.getDebitBalance().compareTo(BigDecimal.ZERO) > 0;
        boolean creditPositive = dto.getCreditBalance() != null
                && dto.getCreditBalance().compareTo(BigDecimal.ZERO) > 0;
        if (debitPositive && creditPositive) {
            throw new BizException("同一科目借方和贷方不能同时大于0");
        }

        SubjectBalance b = new SubjectBalance();
        b.setProjectId(dto.getProjectId());
        b.setSubjectCode(dto.getSubjectCode());
        b.setSubjectName(subject.getSubjectName());
        b.setDebitBalance(dto.getDebitBalance() != null ? dto.getDebitBalance() : BigDecimal.ZERO);
        b.setCreditBalance(dto.getCreditBalance() != null ? dto.getCreditBalance() : BigDecimal.ZERO);
        save(b);
        return b;
    }

    public List<SubjectBalance> listByProject(Long projectId) {
        return list(new LambdaQueryWrapper<SubjectBalance>()
                .eq(SubjectBalance::getProjectId, projectId));
    }

    public TrialBalanceDTO getTrialBalance(Long projectId) {
        List<SubjectBalance> balances = listByProject(projectId);

        BigDecimal totalDebit = balances.stream()
                .map(SubjectBalance::getDebitBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = balances.stream()
                .map(SubjectBalance::getCreditBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TrialBalanceDTO dto = new TrialBalanceDTO();
        dto.setTotalDebit(totalDebit);
        dto.setTotalCredit(totalCredit);
        dto.setBalanced(totalDebit.compareTo(totalCredit) == 0);
        return dto;
    }
}
