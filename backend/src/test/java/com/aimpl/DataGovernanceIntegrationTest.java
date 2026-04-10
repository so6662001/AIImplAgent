package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.finance.entity.AccountSubject;
import com.aimpl.domain.finance.entity.BankAccount;
import com.aimpl.domain.finance.mapper.AccountSubjectMapper;
import com.aimpl.domain.finance.mapper.BankAccountMapper;
import com.aimpl.domain.openingbalance.entity.AccountBalance;
import com.aimpl.domain.openingbalance.entity.SubjectBalance;
import com.aimpl.domain.openingbalance.mapper.AccountBalanceMapper;
import com.aimpl.domain.openingbalance.mapper.SubjectBalanceMapper;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DataGovernanceIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AccountSubjectMapper accountSubjectMapper;
    @Autowired private BankAccountMapper bankAccountMapper;
    @Autowired private SubjectBalanceMapper subjectBalanceMapper;
    @Autowired private AccountBalanceMapper accountBalanceMapper;

    // ========================= Helpers =========================

    private ProjectCreateDTO validProject(String code) {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale("大型");
        dto.setPmId(1L);
        dto.setModules(List.of("采购", "销售"));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("数据治理测试");
        return dto;
    }

    private Long createProject(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validProject(code))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private AccountSubject insertSubject(String code, String name, boolean isLeaf) {
        AccountSubject s = new AccountSubject();
        s.setSubjectCode(code);
        s.setSubjectName(name);
        s.setParentCode("");
        s.setSubjectCategory("资产");
        s.setBalanceDirection("借");
        s.setAuxiliaryAccounting("");
        s.setIsLeaf(isLeaf);
        s.setEnabled(true);
        accountSubjectMapper.insert(s);
        return s;
    }

    private BankAccount insertBankAccount(String code, String name) {
        BankAccount b = new BankAccount();
        b.setAccountCode(code);
        b.setAccountName(name);
        b.setAccountType("BASIC");
        b.setBankName("工商银行");
        b.setBankAccountNo("622202" + System.nanoTime());
        b.setBankBranch("上海分行");
        b.setCurrency("CNY");
        b.setSubjectCode("");
        b.setEnabled(true);
        bankAccountMapper.insert(b);
        return b;
    }

    private SubjectBalance insertSubjectBalance(Long projectId, String code, String name,
                                                 BigDecimal debit, BigDecimal credit) {
        SubjectBalance sb = new SubjectBalance();
        sb.setProjectId(projectId);
        sb.setSubjectCode(code);
        sb.setSubjectName(name);
        sb.setDebitBalance(debit);
        sb.setCreditBalance(credit);
        subjectBalanceMapper.insert(sb);
        return sb;
    }

    private AccountBalance insertAccountBalance(Long projectId, Long bankAccountId,
                                                 BigDecimal amount) {
        AccountBalance ab = new AccountBalance();
        ab.setProjectId(projectId);
        ab.setBankAccountId(bankAccountId);
        ab.setCurrency("CNY");
        ab.setOpeningBalance(amount);
        ab.setRemark("测试");
        accountBalanceMapper.insert(ab);
        return ab;
    }

    // ========================= Test 1: Init import progress =========================

    @Test
    void initImportProgress_creates5BatchesWithCorrectStatuses() throws Exception {
        Long projectId = createProject("DG-INIT");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.currentBatch").value(1))
                .andExpect(jsonPath("$.data.overallStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.batches", hasSize(5)))
                .andExpect(jsonPath("$.data.batches[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.batches[1].status").value("PENDING"))
                .andExpect(jsonPath("$.data.batches[2].status").value("PENDING"))
                .andExpect(jsonPath("$.data.batches[3].status").value("PENDING"))
                .andExpect(jsonPath("$.data.batches[4].status").value("PENDING"));
    }

    // ========================= Test 2: Get progress =========================

    @Test
    void getProgress_returnsBatchNamesAndOverallStatus() throws Exception {
        Long projectId = createProject("DG-PROG");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/data-import/progress/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overallStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.batches[0].batchName").value("基础设置"))
                .andExpect(jsonPath("$.data.batches[1].batchName").value("组织与人员"))
                .andExpect(jsonPath("$.data.batches[2].batchName").value("业务档案"))
                .andExpect(jsonPath("$.data.batches[3].batchName").value("期初余额"))
                .andExpect(jsonPath("$.data.batches[4].batchName").value("税务发票"));
    }

    // ========================= Test 3: Complete batch 1 =========================

    @Test
    void completeBatch1_statusBecomesCompleted() throws Exception {
        Long projectId = createProject("DG-CB1");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/data-import/complete-batch/{projectId}/{batchNumber}", projectId, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.batches[0].status").value("COMPLETED"));
    }

    // ========================= Test 4: Advance to batch 2 =========================

    @Test
    void advanceToBatch2_afterCompletingBatch1() throws Exception {
        Long projectId = createProject("DG-ADV");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/data-import/complete-batch/{projectId}/{batchNumber}", projectId, 1))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/data-import/advance/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.currentBatch").value(2))
                .andExpect(jsonPath("$.data.batches[1].status").value("IN_PROGRESS"));
    }

    // ========================= Test 5: Advance when not completed =========================

    @Test
    void advanceWhenCurrentNotCompleted_returns400() throws Exception {
        Long projectId = createProject("DG-ADVFAIL");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/data-import/advance/{projectId}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("当前批次尚未完成")));
    }

    // ========================= Test 6: Overview lists 22 items =========================

    @Test
    void importOverview_lists22ItemsWithCategories() throws Exception {
        Long projectId = createProject("DG-OV22");

        mockMvc.perform(get("/api/data-import/overview/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalItems").value(22))
                .andExpect(jsonPath("$.data.items", hasSize(22)))
                .andExpect(jsonPath("$.data.items[?(@.category == 'A')]", hasSize(5)))
                .andExpect(jsonPath("$.data.items[?(@.category == 'B')]", hasSize(7)))
                .andExpect(jsonPath("$.data.items[?(@.category == 'C')]", hasSize(1)))
                .andExpect(jsonPath("$.data.items[?(@.category == 'D')]", hasSize(7)))
                .andExpect(jsonPath("$.data.items[?(@.category == 'E')]", hasSize(2)));
    }

    // ========================= Test 7: Overview completedItems increases =========================

    @Test
    void importOverview_completedItemsIncreasesWhenRecordsExist() throws Exception {
        Long projectId = createProject("DG-OVCOMP");

        MvcResult before = mockMvc.perform(get("/api/data-import/overview/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode beforeData = objectMapper.readTree(before.getResponse().getContentAsString()).path("data");
        int initialCompleted = beforeData.path("completedItems").asInt();

        insertSubject("DG-1001", "库存商品", true);

        MvcResult after = mockMvc.perform(get("/api/data-import/overview/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode afterData = objectMapper.readTree(after.getResponse().getContentAsString()).path("data");
        int newCompleted = afterData.path("completedItems").asInt();

        assert newCompleted > initialCompleted :
                "completedItems should increase after inserting records; was " + initialCompleted + " now " + newCompleted;
    }

    // ========================= Test 8: Reconciliation with no data =========================

    @Test
    void reconciliationWithNoData_allLeftValuesAreZero() throws Exception {
        Long projectId = createProject("DG-RECON0");

        mockMvc.perform(get("/api/data-import/reconciliation/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items", hasSize(9)))
                .andExpect(jsonPath("$.data.items[0].leftValue").value(0))
                .andExpect(jsonPath("$.data.items[0].rightValue").value(0))
                .andExpect(jsonPath("$.data.allPassed").value(true));
    }

    // ========================= Test 9: Reconciliation with balanced data =========================

    @Test
    void reconciliationWithBalancedData_allPass() throws Exception {
        Long projectId = createProject("DG-RECONBAL");

        BankAccount bank = insertBankAccount("DG-BA1", "测试银行");
        insertAccountBalance(projectId, bank.getId(), new BigDecimal("50000"));

        insertSubjectBalance(projectId, "SB-1001", "银行存款", new BigDecimal("50000"), BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1002", "库存商品", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1003", "应收账款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1004", "预收账款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1005", "应付账款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1006", "预付账款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1007", "其他应收款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1008", "其他应付款", BigDecimal.ZERO, BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-1009", "实收资本", BigDecimal.ZERO, new BigDecimal("50000"));

        mockMvc.perform(get("/api/data-import/reconciliation/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allPassed").value(true))
                .andExpect(jsonPath("$.data.failCount").value(0))
                .andExpect(jsonPath("$.data.items[?(@.result == 'PASS')]", hasSize(9)));
    }

    // ========================= Test 10: Reconciliation with imbalanced data =========================

    @Test
    void reconciliationWithImbalancedData_hasFail() throws Exception {
        Long projectId = createProject("DG-RECONIMB");

        BankAccount bank = insertBankAccount("DG-BA2", "不平衡银行");
        insertAccountBalance(projectId, bank.getId(), new BigDecimal("100000"));

        insertSubjectBalance(projectId, "SB-2001", "银行存款", new BigDecimal("50000"), BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-2009", "实收资本", BigDecimal.ZERO, new BigDecimal("50000"));

        mockMvc.perform(get("/api/data-import/reconciliation/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.allPassed").value(false))
                .andExpect(jsonPath("$.data.failCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[?(@.result == 'FAIL')]", not(empty())));
    }

    // ========================= Test 11: Complete batch 4 with imbalanced data =========================

    @Test
    void completeBatch4WithImbalancedData_failsWithError() throws Exception {
        Long projectId = createProject("DG-CB4FAIL");

        mockMvc.perform(post("/api/data-import/init/{projectId}", projectId))
                .andExpect(status().isOk());

        for (int batch = 1; batch <= 3; batch++) {
            mockMvc.perform(post("/api/data-import/complete-batch/{projectId}/{batchNumber}", projectId, batch))
                    .andExpect(status().isOk());
            if (batch < 3) {
                mockMvc.perform(post("/api/data-import/advance/{projectId}", projectId))
                        .andExpect(status().isOk());
            }
        }
        mockMvc.perform(post("/api/data-import/advance/{projectId}", projectId))
                .andExpect(status().isOk());

        BankAccount bank = insertBankAccount("DG-BA3", "不平衡银行");
        insertAccountBalance(projectId, bank.getId(), new BigDecimal("99999"));
        insertSubjectBalance(projectId, "SB-3001", "银行存款", new BigDecimal("11111"), BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-3009", "实收资本", BigDecimal.ZERO, new BigDecimal("11111"));

        mockMvc.perform(post("/api/data-import/complete-batch/{projectId}/{batchNumber}", projectId, 4))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batches[3].status").value("FAILED"))
                .andExpect(jsonPath("$.data.lastError", containsString("期初余额对账未通过")));
    }

    // ========================= Test 12: Overview includes reconciliation =========================

    @Test
    void overviewIncludesReconciliation_whenBatch4DataExists() throws Exception {
        Long projectId = createProject("DG-OVREC");

        insertSubjectBalance(projectId, "SB-4001", "库存商品", new BigDecimal("1000"), BigDecimal.ZERO);
        insertSubjectBalance(projectId, "SB-4009", "实收资本", BigDecimal.ZERO, new BigDecimal("1000"));

        mockMvc.perform(get("/api/data-import/overview/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reconciliation").exists())
                .andExpect(jsonPath("$.data.reconciliation.items", hasSize(9)));
    }
}
