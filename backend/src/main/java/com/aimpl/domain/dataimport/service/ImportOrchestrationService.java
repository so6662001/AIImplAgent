package com.aimpl.domain.dataimport.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.archive.service.*;
import com.aimpl.domain.auth.mapper.SysUserMapper;
import com.aimpl.domain.dataimport.entity.ImportProgress;
import com.aimpl.domain.dataimport.mapper.ImportProgressMapper;
import com.aimpl.domain.dataimport.vo.ImportProgressVO;
import com.aimpl.domain.dataimport.vo.ImportProgressVO.BatchStatusVO;
import com.aimpl.domain.dataimport.vo.ReconciliationResultVO;
import com.aimpl.domain.finance.service.AccountSubjectService;
import com.aimpl.domain.finance.service.BankAccountService;
import com.aimpl.domain.openingbalance.service.*;
import com.aimpl.domain.org.service.DepartmentService;
import com.aimpl.domain.org.service.EmployeeService;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportOrchestrationService extends ServiceImpl<ImportProgressMapper, ImportProgress> {

    private static final String PENDING = "PENDING";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final String FAILED = "FAILED";
    private static final String NOT_STARTED = "NOT_STARTED";

    private static final List<String> BATCH_NAMES = List.of(
            "基础设置", "组织与人员", "业务档案", "期初余额", "税务发票"
    );
    private static final List<List<String>> BATCH_ITEMS = List.of(
            List.of("企业资料", "财务科目", "账号资料"),
            List.of("部门", "员工", "用户"),
            List.of("客户", "供应商", "货品类别", "货品方案", "仓库", "库位", "相关单位"),
            List.of("库存余额", "客户往来", "供应商往来", "账户余额", "科目余额", "其他应收", "其他应付"),
            List.of("销项发票", "进项发票")
    );

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

    @Transactional
    public ImportProgressVO initProgress(Long projectId) {
        if (projectMapper.selectById(projectId) == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        ImportProgress existing = getByProjectId(projectId);
        if (existing != null) {
            throw new BizException("该项目已初始化导入进度: " + projectId);
        }

        ImportProgress p = new ImportProgress();
        p.setProjectId(projectId);
        p.setCurrentBatch(1);
        p.setBatch1Status(IN_PROGRESS);
        p.setBatch2Status(PENDING);
        p.setBatch3Status(PENDING);
        p.setBatch4Status(PENDING);
        p.setBatch5Status(PENDING);
        p.setOverallStatus(IN_PROGRESS);
        save(p);
        return toVO(p);
    }

    public ImportProgressVO getProgress(Long projectId) {
        ImportProgress p = getByProjectId(projectId);
        if (p == null) {
            throw new BizException("导入进度不存在，请先初始化: " + projectId);
        }
        return toVO(p);
    }

    @Transactional
    public ImportProgressVO completeBatch(Long projectId, int batchNumber) {
        if (batchNumber < 1 || batchNumber > 5) {
            throw new BizException("批次号无效，必须为1-5: " + batchNumber);
        }

        ImportProgress p = getByProjectId(projectId);
        if (p == null) {
            throw new BizException("导入进度不存在，请先初始化: " + projectId);
        }

        if (p.getCurrentBatch() != batchNumber) {
            throw new BizException("只能完成当前批次(" + p.getCurrentBatch() + ")，不能完成批次: " + batchNumber);
        }

        if (batchNumber == 4) {
            ReconciliationResultVO result = reconciliationService.reconcile(projectId);
            if (!result.isAllPassed()) {
                setBatchStatus(p, batchNumber, FAILED);
                p.setLastError("期初余额对账未通过: " + result.getFailCount() + "项不平衡");
                updateById(p);
                return toVO(p);
            }
        }

        setBatchStatus(p, batchNumber, COMPLETED);
        p.setLastError(null);

        if (batchNumber == 5) {
            p.setOverallStatus(COMPLETED);
        }

        updateById(p);
        return toVO(p);
    }

    @Transactional
    public ImportProgressVO advanceBatch(Long projectId) {
        ImportProgress p = getByProjectId(projectId);
        if (p == null) {
            throw new BizException("导入进度不存在，请先初始化: " + projectId);
        }

        int current = p.getCurrentBatch();
        if (current >= 5) {
            throw new BizException("已经是最后一个批次，无法推进");
        }

        String currentStatus = getBatchStatus(p, current);
        if (!COMPLETED.equals(currentStatus)) {
            throw new BizException("当前批次尚未完成，不能推进到下一批次");
        }

        int next = current + 1;
        p.setCurrentBatch(next);
        setBatchStatus(p, next, IN_PROGRESS);
        updateById(p);
        return toVO(p);
    }

    private ImportProgress getByProjectId(Long projectId) {
        return getOne(new LambdaQueryWrapper<ImportProgress>()
                .eq(ImportProgress::getProjectId, projectId));
    }

    private String getBatchStatus(ImportProgress p, int batch) {
        return switch (batch) {
            case 1 -> p.getBatch1Status();
            case 2 -> p.getBatch2Status();
            case 3 -> p.getBatch3Status();
            case 4 -> p.getBatch4Status();
            case 5 -> p.getBatch5Status();
            default -> throw new BizException("批次号无效: " + batch);
        };
    }

    private void setBatchStatus(ImportProgress p, int batch, String status) {
        switch (batch) {
            case 1 -> p.setBatch1Status(status);
            case 2 -> p.setBatch2Status(status);
            case 3 -> p.setBatch3Status(status);
            case 4 -> p.setBatch4Status(status);
            case 5 -> p.setBatch5Status(status);
            default -> throw new BizException("批次号无效: " + batch);
        }
    }

    private ImportProgressVO toVO(ImportProgress p) {
        ImportProgressVO vo = new ImportProgressVO();
        vo.setProjectId(p.getProjectId());
        vo.setCurrentBatch(p.getCurrentBatch());
        vo.setOverallStatus(p.getOverallStatus());
        vo.setLastError(p.getLastError());

        List<BatchStatusVO> batches = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            BatchStatusVO b = new BatchStatusVO();
            b.setBatchNumber(i);
            b.setBatchName(BATCH_NAMES.get(i - 1));
            b.setStatus(getBatchStatus(p, i));
            b.setItems(BATCH_ITEMS.get(i - 1));
            b.setItemCount(countBatchRecords(p.getProjectId(), i));
            batches.add(b);
        }
        vo.setBatches(batches);
        return vo;
    }

    int countBatchRecords(Long projectId, int batch) {
        return switch (batch) {
            case 1 -> (int) (accountSubjectService.count()
                    + bankAccountService.count());
            case 2 -> (int) (departmentService.count()
                    + employeeService.count()
                    + sysUserMapper.selectCount(null));
            case 3 -> (int) (customerService.count()
                    + supplierService.count()
                    + productCategoryService.count()
                    + productService.count()
                    + warehouseService.count()
                    + storageLocationService.count()
                    + relatedUnitService.count());
            case 4 -> (int) (inventoryBalanceService.count()
                    + customerBalanceService.count()
                    + supplierBalanceService.count()
                    + accountBalanceService.count()
                    + subjectBalanceService.count()
                    + otherReceivableService.count()
                    + otherPayableService.count());
            case 5 -> (int) invoiceBalanceService.count();
            default -> 0;
        };
    }
}
