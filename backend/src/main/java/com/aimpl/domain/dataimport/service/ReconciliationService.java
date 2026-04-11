package com.aimpl.domain.dataimport.service;

import com.aimpl.domain.openingbalance.dto.TrialBalanceDTO;
import com.aimpl.domain.openingbalance.entity.*;
import com.aimpl.domain.openingbalance.service.*;
import com.aimpl.domain.dataimport.vo.ReconciliationResultVO;
import com.aimpl.domain.dataimport.vo.ReconciliationResultVO.ReconciliationItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private static final BigDecimal ROUNDING_TOLERANCE = new BigDecimal("0.01");

    private final SubjectBalanceService subjectBalanceService;
    private final InventoryBalanceService inventoryBalanceService;
    private final CustomerBalanceService customerBalanceService;
    private final SupplierBalanceService supplierBalanceService;
    private final AccountBalanceService accountBalanceService;
    private final OtherReceivableService otherReceivableService;
    private final OtherPayableService otherPayableService;

    public ReconciliationResultVO reconcile(Long projectId) {
        List<SubjectBalance> subjectBalances = subjectBalanceService.listByProject(projectId);
        List<ReconciliationItemVO> items = new ArrayList<>();

        items.add(checkTrialBalance(projectId));
        items.add(checkInventoryVsSubject(projectId, subjectBalances));
        items.add(checkCustomerReceivableVsSubject(projectId, subjectBalances));
        items.add(checkCustomerPrepaidVsSubject(projectId, subjectBalances));
        items.add(checkSupplierPayableVsSubject(projectId, subjectBalances));
        items.add(checkSupplierPrepaidVsSubject(projectId, subjectBalances));
        items.add(checkBankBalanceVsSubject(projectId, subjectBalances));
        items.add(checkOtherReceivableVsSubject(projectId, subjectBalances));
        items.add(checkOtherPayableVsSubject(projectId, subjectBalances));

        int passCount = 0, warnCount = 0, failCount = 0;
        for (ReconciliationItemVO item : items) {
            switch (item.getResult()) {
                case "PASS" -> passCount++;
                case "WARNING" -> warnCount++;
                case "FAIL" -> failCount++;
            }
        }

        ReconciliationResultVO result = new ReconciliationResultVO();
        result.setProjectId(projectId);
        result.setCheckedAt(LocalDateTime.now());
        result.setAllPassed(failCount == 0);
        result.setPassCount(passCount);
        result.setWarnCount(warnCount);
        result.setFailCount(failCount);
        result.setItems(items);
        return result;
    }

    private ReconciliationItemVO checkTrialBalance(Long projectId) {
        TrialBalanceDTO tb = subjectBalanceService.getTrialBalance(projectId);
        return buildItem(
                "试算平衡",
                "Σ借方余额", tb.getTotalDebit(),
                "Σ贷方余额", tb.getTotalCredit()
        );
    }

    private ReconciliationItemVO checkInventoryVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal inventoryTotal = inventoryBalanceService.listByProject(projectId).stream()
                .map(InventoryBalance::getCostAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectDebit(subjects, "库存商品");

        return buildItem(
                "库存金额 vs 库存商品科目",
                "Σ库存期初数.成本金额", inventoryTotal,
                "科目余额.库存商品", subjectAmount
        );
    }

    private ReconciliationItemVO checkCustomerReceivableVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal positiveBalance = customerBalanceService.listByProject(projectId).stream()
                .map(CustomerBalance::getBalance)
                .filter(b -> b != null && b.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectDebit(subjects, "应收账款");

        return buildItem(
                "客户应收余额 vs 应收账款科目",
                "Σ客户余额(正)", positiveBalance,
                "科目余额.应收账款", subjectAmount
        );
    }

    private ReconciliationItemVO checkCustomerPrepaidVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal negativeBalance = customerBalanceService.listByProject(projectId).stream()
                .map(CustomerBalance::getBalance)
                .filter(b -> b != null && b.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectCredit(subjects, "预收账款");

        return buildItem(
                "客户预收余额 vs 预收账款科目",
                "Σ|客户余额(负)|", negativeBalance,
                "科目余额.预收账款", subjectAmount
        );
    }

    private ReconciliationItemVO checkSupplierPayableVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal positiveBalance = supplierBalanceService.listByProject(projectId).stream()
                .map(SupplierBalance::getBalance)
                .filter(b -> b != null && b.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectCredit(subjects, "应付账款");

        return buildItem(
                "供应商应付余额 vs 应付账款科目",
                "Σ供应商余额(正)", positiveBalance,
                "科目余额.应付账款", subjectAmount
        );
    }

    private ReconciliationItemVO checkSupplierPrepaidVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal negativeBalance = supplierBalanceService.listByProject(projectId).stream()
                .map(SupplierBalance::getBalance)
                .filter(b -> b != null && b.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectDebit(subjects, "预付账款");

        return buildItem(
                "供应商预付余额 vs 预付账款科目",
                "Σ|供应商余额(负)|", negativeBalance,
                "科目余额.预付账款", subjectAmount
        );
    }

    private ReconciliationItemVO checkBankBalanceVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal bankTotal = accountBalanceService.listByProject(projectId).stream()
                .map(AccountBalance::getOpeningBalance)
                .filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectDebit(subjects, "银行存款");

        return buildItem(
                "银行账户余额 vs 银行存款科目",
                "Σ账户余额", bankTotal,
                "科目余额.银行存款", subjectAmount
        );
    }

    private ReconciliationItemVO checkOtherReceivableVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal orTotal = otherReceivableService.listByProject(projectId).stream()
                .map(OtherReceivable::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectDebit(subjects, "其他应收款");

        return buildItem(
                "其他应收合计 vs 其他应收款科目",
                "Σ其他应收金额", orTotal,
                "科目余额.其他应收款", subjectAmount
        );
    }

    private ReconciliationItemVO checkOtherPayableVsSubject(Long projectId, List<SubjectBalance> subjects) {
        BigDecimal opTotal = otherPayableService.listByProject(projectId).stream()
                .map(OtherPayable::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subjectAmount = sumSubjectCredit(subjects, "其他应付款");

        return buildItem(
                "其他应付合计 vs 其他应付款科目",
                "Σ其他应付金额", opTotal,
                "科目余额.其他应付款", subjectAmount
        );
    }

    private BigDecimal sumSubjectDebit(List<SubjectBalance> subjects, String namePattern) {
        return subjects.stream()
                .filter(s -> s.getSubjectName() != null && s.getSubjectName().contains(namePattern))
                .map(SubjectBalance::getDebitBalance)
                .filter(d -> d != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumSubjectCredit(List<SubjectBalance> subjects, String namePattern) {
        return subjects.stream()
                .filter(s -> s.getSubjectName() != null && s.getSubjectName().contains(namePattern))
                .map(SubjectBalance::getCreditBalance)
                .filter(c -> c != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ReconciliationItemVO buildItem(String checkName,
                                           String leftLabel, BigDecimal leftValue,
                                           String rightLabel, BigDecimal rightValue) {
        ReconciliationItemVO item = new ReconciliationItemVO();
        item.setCheckName(checkName);
        item.setLeftLabel(leftLabel);
        item.setLeftValue(leftValue != null ? leftValue : BigDecimal.ZERO);
        item.setRightLabel(rightLabel);
        item.setRightValue(rightValue != null ? rightValue : BigDecimal.ZERO);

        BigDecimal diff = item.getLeftValue().subtract(item.getRightValue()).abs();
        item.setDifference(diff);

        if (diff.compareTo(BigDecimal.ZERO) == 0) {
            item.setResult("PASS");
            item.setRemark("完全一致");
        } else if (diff.compareTo(ROUNDING_TOLERANCE) <= 0) {
            item.setResult("WARNING");
            item.setRemark("差异在舍入容差范围内(" + ROUNDING_TOLERANCE + ")");
        } else {
            item.setResult("FAIL");
            item.setRemark("差异: " + diff);
        }
        return item;
    }
}
