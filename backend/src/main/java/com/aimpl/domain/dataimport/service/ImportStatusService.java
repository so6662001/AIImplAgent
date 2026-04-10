package com.aimpl.domain.dataimport.service;

import com.aimpl.domain.archive.service.*;
import com.aimpl.domain.auth.mapper.SysUserMapper;
import com.aimpl.domain.dataimport.entity.ImportProgress;
import com.aimpl.domain.dataimport.vo.ImportStatusOverviewVO;
import com.aimpl.domain.dataimport.vo.ImportStatusOverviewVO.ImportItemStatusVO;
import com.aimpl.domain.dataimport.vo.ReconciliationResultVO;
import com.aimpl.domain.finance.service.AccountSubjectService;
import com.aimpl.domain.finance.service.BankAccountService;
import com.aimpl.domain.openingbalance.entity.InvoiceBalance;
import com.aimpl.domain.openingbalance.service.*;
import com.aimpl.domain.org.service.DepartmentService;
import com.aimpl.domain.org.service.EmployeeService;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportStatusService {

    private final ProjectMapper projectMapper;
    private final AccountSubjectService accountSubjectService;
    private final BankAccountService bankAccountService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final SysUserMapper sysUserMapper;
    private final CustomerService customerService;
    private final SupplierService supplierService;
    private final ProductCategoryService productCategoryService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final StorageLocationService storageLocationService;
    private final RelatedUnitService relatedUnitService;
    private final InventoryBalanceService inventoryBalanceService;
    private final CustomerBalanceService customerBalanceService;
    private final SupplierBalanceService supplierBalanceService;
    private final AccountBalanceService accountBalanceService;
    private final SubjectBalanceService subjectBalanceService;
    private final OtherReceivableService otherReceivableService;
    private final OtherPayableService otherPayableService;
    private final InvoiceBalanceService invoiceBalanceService;
    private final ReconciliationService reconciliationService;
    private final ImportOrchestrationService importOrchestrationService;

    public ImportStatusOverviewVO getOverview(Long projectId) {
        List<ImportItemStatusVO> items = new ArrayList<>();

        items.add(buildItem("A", "A1", "企业资料", countProjects(projectId), 1));
        items.add(buildItem("A", "A2", "部门", (int) departmentService.count(), 2));
        items.add(buildItem("A", "A3", "员工", (int) employeeService.count(), 2));
        items.add(buildItem("A", "A4", "用户", sysUserMapper.selectCount(null).intValue(), 2));
        items.add(buildItem("A", "A5", "账号资料", (int) bankAccountService.count(), 1));

        items.add(buildItem("B", "B1", "客户档案", (int) customerService.count(), 3));
        items.add(buildItem("B", "B2", "供应商档案", (int) supplierService.count(), 3));
        items.add(buildItem("B", "B3", "货品类别", (int) productCategoryService.count(), 3));
        items.add(buildItem("B", "B4", "货品方案", (int) productService.count(), 3));
        items.add(buildItem("B", "B5", "仓库", (int) warehouseService.count(), 3));
        items.add(buildItem("B", "B6", "库位", (int) storageLocationService.count(), 3));
        items.add(buildItem("B", "B7", "相关单位", (int) relatedUnitService.count(), 3));

        items.add(buildItem("C", "C1", "财务科目", (int) accountSubjectService.count(), 1));

        items.add(buildItem("D", "D1", "库存期初", (int) inventoryBalanceService.count(), 4));
        items.add(buildItem("D", "D2", "客户往来", (int) customerBalanceService.count(), 4));
        items.add(buildItem("D", "D3", "供应商往来", (int) supplierBalanceService.count(), 4));
        items.add(buildItem("D", "D4", "账户余额", (int) accountBalanceService.count(), 4));
        items.add(buildItem("D", "D5", "科目余额", (int) subjectBalanceService.count(), 4));
        items.add(buildItem("D", "D6", "其他应收", (int) otherReceivableService.count(), 4));
        items.add(buildItem("D", "D7", "其他应付", (int) otherPayableService.count(), 4));

        int salesInvoiceCount = (int) invoiceBalanceService.count(
                new LambdaQueryWrapper<InvoiceBalance>()
                        .eq(InvoiceBalance::getInvoiceType, "SALES_OUTPUT"));
        int purchaseInvoiceCount = (int) invoiceBalanceService.count(
                new LambdaQueryWrapper<InvoiceBalance>()
                        .eq(InvoiceBalance::getInvoiceType, "PURCHASE_INPUT"));
        items.add(buildItem("E", "E1", "销项发票", salesInvoiceCount, 5));
        items.add(buildItem("E", "E2", "进项发票", purchaseInvoiceCount, 5));

        int totalItems = items.size();
        int completedItems = (int) items.stream().filter(i -> i.getRecordCount() > 0).count();
        BigDecimal completionRate = totalItems > 0
                ? BigDecimal.valueOf(completedItems)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalItems), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String overallStatus;
        if (completedItems == 0) {
            overallStatus = "未开始";
        } else if (completedItems < totalItems) {
            overallStatus = "进行中";
        } else {
            overallStatus = "已完成";
        }

        ImportStatusOverviewVO vo = new ImportStatusOverviewVO();
        vo.setProjectId(projectId);
        vo.setTotalItems(totalItems);
        vo.setCompletedItems(completedItems);
        vo.setCompletionRate(completionRate);
        vo.setOverallStatus(overallStatus);
        vo.setItems(items);

        boolean batch4Done = items.stream()
                .filter(i -> "D".equals(i.getCategory()))
                .anyMatch(i -> i.getRecordCount() > 0);
        if (batch4Done) {
            try {
                vo.setReconciliation(reconciliationService.reconcile(projectId));
            } catch (Exception ignored) {
            }
        }

        return vo;
    }

    private int countProjects(Long projectId) {
        return projectMapper.selectById(projectId) != null ? 1 : 0;
    }

    private ImportItemStatusVO buildItem(String category, String itemCode, String itemName,
                                         int recordCount, int batchNumber) {
        ImportItemStatusVO item = new ImportItemStatusVO();
        item.setCategory(category);
        item.setItemCode(itemCode);
        item.setItemName(itemName);
        item.setRecordCount(recordCount);
        item.setStatus(recordCount > 0 ? "已导入" : "未导入");
        item.setBatchNumber(batchNumber);
        return item;
    }
}
