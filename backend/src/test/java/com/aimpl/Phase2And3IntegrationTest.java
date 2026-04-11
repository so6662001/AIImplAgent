package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.accountset.dto.AccountSetCreateDTO;
import com.aimpl.domain.golive.dto.GoLiveCheckUpdateDTO;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.report.dto.DeliveryReportCreateDTO;
import com.aimpl.domain.research.dto.CustomerProfileCreateDTO;
import com.aimpl.domain.server.dto.ServerProfileCreateDTO;
import com.aimpl.domain.simulation.dto.SimulationExecuteDTO;
import com.aimpl.domain.simulation.dto.SimulationSceneCreateDTO;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
import com.aimpl.domain.workforce.dto.ProjectEvaluationCreateDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Phase2And3IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================= Helpers =========================

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private ProjectCreateDTO validProjectDTO(String code) {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale("大型");
        dto.setPmId(1L);
        dto.setModules(List.of("采购", "销售"));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("集成测试");
        return dto;
    }

    private Long createProject(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validProjectDTO(code))))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private CustomerProfileCreateDTO validCustomerProfileDTO(Long projectId) {
        CustomerProfileCreateDTO dto = new CustomerProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setCompanyName("测试钢贸公司");
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setTotalStaff(200);
        dto.setTotalProductionLines(3);
        dto.setMonthlyVolume(new BigDecimal("5000"));
        dto.setMonthlyAmount(new BigDecimal("20000000"));
        dto.setManagementGoals("提升管理效率");
        return dto;
    }

    private AccountSetCreateDTO validAccountSetDTO(Long projectId) {
        AccountSetCreateDTO dto = new AccountSetCreateDTO();
        dto.setProjectId(projectId);
        dto.setSetName("主帐套");
        dto.setQtyDecimals(2);
        dto.setWgtDecimals(3);
        dto.setPrcDecimals(2);
        dto.setAmtDecimals(2);
        dto.setFiscalYearStart(1);
        return dto;
    }

    private DeliveryReportCreateDTO validDeliveryReportDTO(Long projectId) {
        DeliveryReportCreateDTO dto = new DeliveryReportCreateDTO();
        dto.setProjectId(projectId);
        dto.setTitle("里程碑报告-采购模块");
        dto.setReportType("MILESTONE");
        dto.setTrainingPassRate(new BigDecimal("85.5"));
        dto.setDataImportCompletionRate(new BigDecimal("90"));
        dto.setTotalIssues(20);
        dto.setResolvedIssues(18);
        dto.setCustomerSatisfactionScore(new BigDecimal("4.5"));
        dto.setScheduleDeviationPercent(new BigDecimal("5"));
        dto.setDocumentCompletionRate(new BigDecimal("80"));
        return dto;
    }

    private EngineerCreateDTO validEngineerDTO(String code, String name) {
        EngineerCreateDTO dto = new EngineerCreateDTO();
        dto.setEngineerCode(code);
        dto.setName(name);
        dto.setLevel("SENIOR");
        dto.setSkills("Java,SQL");
        dto.setJoinDate(LocalDate.of(2023, 1, 1));
        dto.setPhone("13800138001");
        dto.setEmail("test@example.com");
        return dto;
    }

    private ProjectEvaluationCreateDTO validEvaluationDTO(Long projectId) {
        ProjectEvaluationCreateDTO dto = new ProjectEvaluationCreateDTO();
        dto.setProjectId(projectId);
        dto.setScheduleScore(80);
        dto.setQualityScore(90);
        dto.setCsatScore(85);
        dto.setProcessScore(70);
        dto.setCostScore(75);
        dto.setAiComment("整体良好");
        return dto;
    }

    private ServerProfileCreateDTO validServerProfileDTO(Long projectId) {
        ServerProfileCreateDTO dto = new ServerProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setServerName("生产服务器");
        dto.setHost("192.168.1.100");
        dto.setPort(3306);
        dto.setDbName("erp_prod");
        return dto;
    }

    private SimulationSceneCreateDTO validSimulationSceneDTO(Long projectId) {
        SimulationSceneCreateDTO dto = new SimulationSceneCreateDTO();
        dto.setProjectId(projectId);
        dto.setSceneName("采购入库流程");
        dto.setSceneType("PURCHASE_INBOUND");
        dto.setDescription("模拟采购入库全流程");
        dto.setSteps("1.创建采购单 2.审批 3.入库 4.对账");
        dto.setExpectedResult("流程顺利完成");
        return dto;
    }

    private Long createAndGetId(String url, Object dto) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    // ===================== CustomerProfile Tests =====================

    @Test
    void customerProfile_happyPath() throws Exception {
        Long projectId = createProject("CP-001");
        CustomerProfileCreateDTO dto = validCustomerProfileDTO(projectId);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.companyName").value("测试钢贸公司"))
                .andExpect(jsonPath("$.data.totalStaff").value(200))
                .andExpect(jsonPath("$.data.totalProductionLines").value(3));
    }

    @Test
    void customerProfile_getById() throws Exception {
        Long projectId = createProject("CP-002");
        Long cpId = createAndGetId("/api/customer-profiles", validCustomerProfileDTO(projectId));

        mockMvc.perform(get("/api/customer-profiles/{id}", cpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(cpId.intValue()))
                .andExpect(jsonPath("$.data.companyName").value("测试钢贸公司"));
    }

    @Test
    void customerProfile_listByProject() throws Exception {
        Long projectId = createProject("CP-003");
        CustomerProfileCreateDTO dto1 = validCustomerProfileDTO(projectId);
        dto1.setCompanyName("公司A");
        createAndGetId("/api/customer-profiles", dto1);

        CustomerProfileCreateDTO dto2 = validCustomerProfileDTO(projectId);
        dto2.setCompanyName("公司B");
        createAndGetId("/api/customer-profiles", dto2);

        mockMvc.perform(get("/api/customer-profiles").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void customerProfile_blankCompanyName_returns400() throws Exception {
        Long projectId = createProject("CP-V1");
        CustomerProfileCreateDTO dto = validCustomerProfileDTO(projectId);
        dto.setCompanyName("");

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("公司名称不能为空")));
    }

    @Test
    void customerProfile_nullProjectId_returns400() throws Exception {
        CustomerProfileCreateDTO dto = validCustomerProfileDTO(1L);
        dto.setProjectId(null);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    @Test
    void customerProfile_negativeStaff_returns400() throws Exception {
        Long projectId = createProject("CP-V2");
        CustomerProfileCreateDTO dto = validCustomerProfileDTO(projectId);
        dto.setTotalStaff(-1);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("员工数量不能为负数")));
    }

    @Test
    void customerProfile_invalidProjectId_returns400() throws Exception {
        CustomerProfileCreateDTO dto = validCustomerProfileDTO(999999L);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    @Test
    void customerProfile_duplicateNameInProject_returns400() throws Exception {
        Long projectId = createProject("CP-DUP");
        createAndGetId("/api/customer-profiles", validCustomerProfileDTO(projectId));

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validCustomerProfileDTO(projectId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目下公司名称已存在")));
    }

    // ===================== AccountSet Tests =====================

    @Test
    void accountSet_happyPath() throws Exception {
        Long projectId = createProject("AS-001");
        AccountSetCreateDTO dto = validAccountSetDTO(projectId);

        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.setName").value("主帐套"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.currency").value("CNY"))
                .andExpect(jsonPath("$.data.wgtDecimals").value(3));
    }

    @Test
    void accountSet_getById() throws Exception {
        Long projectId = createProject("AS-002");
        Long asId = createAndGetId("/api/account-sets", validAccountSetDTO(projectId));

        mockMvc.perform(get("/api/account-sets/{id}", asId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(asId.intValue()))
                .andExpect(jsonPath("$.data.setName").value("主帐套"));
    }

    @Test
    void accountSet_listByProject() throws Exception {
        Long projectId = createProject("AS-003");
        createAndGetId("/api/account-sets", validAccountSetDTO(projectId));

        mockMvc.perform(get("/api/account-sets").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void accountSet_blankSetName_returns400() throws Exception {
        Long projectId = createProject("AS-V1");
        AccountSetCreateDTO dto = validAccountSetDTO(projectId);
        dto.setSetName("");

        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("帐套名称不能为空")));
    }

    @Test
    void accountSet_nullProjectId_returns400() throws Exception {
        AccountSetCreateDTO dto = validAccountSetDTO(1L);
        dto.setProjectId(null);

        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目ID不能为空")));
    }

    @Test
    void accountSet_amtDecimalsExceedsMax_returns400() throws Exception {
        Long projectId = createProject("AS-V2");
        AccountSetCreateDTO dto = validAccountSetDTO(projectId);
        dto.setAmtDecimals(5);

        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("金额小数位不能超过2")));
    }

    @Test
    void accountSet_activateDraftToActive() throws Exception {
        Long projectId = createProject("AS-ACT");
        Long asId = createAndGetId("/api/account-sets", validAccountSetDTO(projectId));

        mockMvc.perform(put("/api/account-sets/{id}/activate", asId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void accountSet_activateAlreadyActive_returns400() throws Exception {
        Long projectId = createProject("AS-ACT2");
        Long asId = createAndGetId("/api/account-sets", validAccountSetDTO(projectId));

        mockMvc.perform(put("/api/account-sets/{id}/activate", asId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/account-sets/{id}/activate", asId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("只有草稿状态的帐套才能启用")));
    }

    @Test
    void accountSet_duplicatePerProject_returns400() throws Exception {
        Long projectId = createProject("AS-DUP");
        createAndGetId("/api/account-sets", validAccountSetDTO(projectId));

        AccountSetCreateDTO dto2 = validAccountSetDTO(projectId);
        dto2.setSetName("第二帐套");
        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目已存在帐套")));
    }

    @Test
    void accountSet_invalidProject_returns400() throws Exception {
        AccountSetCreateDTO dto = validAccountSetDTO(999999L);

        mockMvc.perform(post("/api/account-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ===================== DeliveryReport Tests =====================

    @Test
    void deliveryReport_happyPath() throws Exception {
        Long projectId = createProject("DR-001");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.title").value("里程碑报告-采购模块"))
                .andExpect(jsonPath("$.data.reportType").value("MILESTONE"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.overallScore").isNumber());
    }

    @Test
    void deliveryReport_getById() throws Exception {
        Long projectId = createProject("DR-002");
        Long drId = createAndGetId("/api/delivery-reports", validDeliveryReportDTO(projectId));

        mockMvc.perform(get("/api/delivery-reports/{id}", drId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(drId.intValue()))
                .andExpect(jsonPath("$.data.title").value("里程碑报告-采购模块"));
    }

    @Test
    void deliveryReport_listByProject() throws Exception {
        Long projectId = createProject("DR-003");
        createAndGetId("/api/delivery-reports", validDeliveryReportDTO(projectId));

        mockMvc.perform(get("/api/delivery-reports").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void deliveryReport_blankTitle_returns400() throws Exception {
        Long projectId = createProject("DR-V1");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);
        dto.setTitle("");

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("报告标题不能为空")));
    }

    @Test
    void deliveryReport_blankReportType_returns400() throws Exception {
        Long projectId = createProject("DR-V2");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);
        dto.setReportType("");

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("报告类型不能为空")));
    }

    @Test
    void deliveryReport_trainingPassRateOver100_returns400() throws Exception {
        Long projectId = createProject("DR-V3");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);
        dto.setTrainingPassRate(new BigDecimal("101"));

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("培训通过率不能超过100")));
    }

    @Test
    void deliveryReport_confirm() throws Exception {
        Long projectId = createProject("DR-CNF");
        Long drId = createAndGetId("/api/delivery-reports", validDeliveryReportDTO(projectId));

        mockMvc.perform(put("/api/delivery-reports/{id}/confirm", drId)
                        .param("confirmedBy", "PM张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.confirmedBy").value("PM张三"));
    }

    @Test
    void deliveryReport_doubleConfirm_returns400() throws Exception {
        Long projectId = createProject("DR-CNF2");
        Long drId = createAndGetId("/api/delivery-reports", validDeliveryReportDTO(projectId));

        mockMvc.perform(put("/api/delivery-reports/{id}/confirm", drId))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/delivery-reports/{id}/confirm", drId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("报告已确认，不能重复确认")));
    }

    @Test
    void deliveryReport_totalIssuesLessThanResolved_returns400() throws Exception {
        Long projectId = createProject("DR-ISS");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);
        dto.setTotalIssues(5);
        dto.setResolvedIssues(10);

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("已解决问题数不能大于总问题数")));
    }

    @Test
    void deliveryReport_invalidReportType_returns400() throws Exception {
        Long projectId = createProject("DR-IRT");
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(projectId);
        dto.setReportType("INVALID_TYPE");

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效的报告类型")));
    }

    @Test
    void deliveryReport_invalidProjectId_returns400() throws Exception {
        DeliveryReportCreateDTO dto = validDeliveryReportDTO(999999L);

        mockMvc.perform(post("/api/delivery-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ===================== Engineer Tests =====================

    @Test
    void engineer_happyPath() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-001", "张工");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.engineerCode").value("ENG-001"))
                .andExpect(jsonPath("$.data.name").value("张工"))
                .andExpect(jsonPath("$.data.level").value("SENIOR"))
                .andExpect(jsonPath("$.data.currentStatus").value("IDLE"))
                .andExpect(jsonPath("$.data.monthlyProjectCount").value(0));
    }

    @Test
    void engineer_getById() throws Exception {
        Long engId = createAndGetId("/api/engineers", validEngineerDTO("ENG-002", "李工"));

        mockMvc.perform(get("/api/engineers/{id}", engId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(engId.intValue()))
                .andExpect(jsonPath("$.data.engineerCode").value("ENG-002"));
    }

    @Test
    void engineer_list() throws Exception {
        createAndGetId("/api/engineers", validEngineerDTO("ENG-L1", "工A"));

        EngineerCreateDTO dto2 = validEngineerDTO("ENG-L2", "工B");
        dto2.setPhone("13900139002");
        dto2.setEmail("b@test.com");
        createAndGetId("/api/engineers", dto2);

        mockMvc.perform(get("/api/engineers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void engineer_blankCode_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("", "无编号");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("工程师编号不能为空")));
    }

    @Test
    void engineer_blankName_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-VN", "");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("姓名不能为空")));
    }

    @Test
    void engineer_blankLevel_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-VL", "级别空");
        dto.setLevel("");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("级别不能为空")));
    }

    @Test
    void engineer_invalidPhone_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-VP", "电话错");
        dto.setPhone("12345");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("手机号格式不正确")));
    }

    @Test
    void engineer_invalidEmail_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-VE", "邮箱错");
        dto.setEmail("not-an-email");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("邮箱格式不正确")));
    }

    @Test
    void engineer_duplicateCode_returns400() throws Exception {
        createAndGetId("/api/engineers", validEngineerDTO("ENG-DUP", "工A"));

        EngineerCreateDTO dto2 = validEngineerDTO("ENG-DUP", "工B");
        dto2.setPhone("13900139099");
        dto2.setEmail("dup@test.com");
        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("工程师编号已存在")));
    }

    @Test
    void engineer_invalidLevel_returns400() throws Exception {
        EngineerCreateDTO dto = validEngineerDTO("ENG-IL", "无效级别");
        dto.setLevel("INVALID_LEVEL");

        mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效的工程师级别")));
    }

    // ===================== ProjectEvaluation Tests =====================

    @Test
    void projectEvaluation_happyPath_pqiCalculation() throws Exception {
        Long projectId = createProject("PE-001");
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(projectId);
        // PQI = 80*0.20 + 90*0.30 + 85*0.25 + 70*0.15 + 75*0.10
        //     = 16 + 27 + 21.25 + 10.5 + 7.5 = 82.25
        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.scheduleScore").value(80))
                .andExpect(jsonPath("$.data.qualityScore").value(90))
                .andExpect(jsonPath("$.data.csatScore").value(85))
                .andExpect(jsonPath("$.data.processScore").value(70))
                .andExpect(jsonPath("$.data.costScore").value(75))
                .andExpect(jsonPath("$.data.pqiScore").value(82.25))
                .andExpect(jsonPath("$.data.rating").value("GOOD"));
    }

    @Test
    void projectEvaluation_excellentRating() throws Exception {
        Long projectId = createProject("PE-EXC");
        ProjectEvaluationCreateDTO dto = new ProjectEvaluationCreateDTO();
        dto.setProjectId(projectId);
        dto.setScheduleScore(95);
        dto.setQualityScore(95);
        dto.setCsatScore(95);
        dto.setProcessScore(95);
        dto.setCostScore(95);
        // PQI = 95*(0.20+0.30+0.25+0.15+0.10) = 95*1.00 = 95.00

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pqiScore").value(95.00))
                .andExpect(jsonPath("$.data.rating").value("EXCELLENT"));
    }

    @Test
    void projectEvaluation_unqualifiedRating() throws Exception {
        Long projectId = createProject("PE-UNQ");
        ProjectEvaluationCreateDTO dto = new ProjectEvaluationCreateDTO();
        dto.setProjectId(projectId);
        dto.setScheduleScore(20);
        dto.setQualityScore(30);
        dto.setCsatScore(25);
        dto.setProcessScore(35);
        dto.setCostScore(10);
        // PQI = 20*0.20 + 30*0.30 + 25*0.25 + 35*0.15 + 10*0.10
        //     = 4 + 9 + 6.25 + 5.25 + 1 = 25.50

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pqiScore").value(25.50))
                .andExpect(jsonPath("$.data.rating").value("UNQUALIFIED"));
    }

    @Test
    void projectEvaluation_listByProject() throws Exception {
        Long projectId = createProject("PE-LST");
        createAndGetId("/api/project-evaluations", validEvaluationDTO(projectId));

        mockMvc.perform(get("/api/project-evaluations").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void projectEvaluation_nullScheduleScore_returns400() throws Exception {
        Long projectId = createProject("PE-V1");
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(projectId);
        dto.setScheduleScore(null);

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("进度评分不能为空")));
    }

    @Test
    void projectEvaluation_nullQualityScore_returns400() throws Exception {
        Long projectId = createProject("PE-V2");
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(projectId);
        dto.setQualityScore(null);

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("质量评分不能为空")));
    }

    @Test
    void projectEvaluation_scoreOver100_returns400() throws Exception {
        Long projectId = createProject("PE-V3");
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(projectId);
        dto.setCsatScore(101);

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("客户满意度评分不能超过100")));
    }

    @Test
    void projectEvaluation_negativeScore_returns400() throws Exception {
        Long projectId = createProject("PE-V4");
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(projectId);
        dto.setCostScore(-1);

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("成本评分不能为负数")));
    }

    @Test
    void projectEvaluation_invalidProject_returns400() throws Exception {
        ProjectEvaluationCreateDTO dto = validEvaluationDTO(999999L);

        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ===================== ServerProfile Tests =====================

    @Test
    void serverProfile_happyPath() throws Exception {
        Long projectId = createProject("SP-001");
        ServerProfileCreateDTO dto = validServerProfileDTO(projectId);

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.serverName").value("生产服务器"))
                .andExpect(jsonPath("$.data.host").value("192.168.1.100"))
                .andExpect(jsonPath("$.data.port").value(3306))
                .andExpect(jsonPath("$.data.dbName").value("erp_prod"))
                .andExpect(jsonPath("$.data.status").value("UNCONFIGURED"))
                .andExpect(jsonPath("$.data.sslEnabled").value(false));
    }

    @Test
    void serverProfile_getById() throws Exception {
        Long projectId = createProject("SP-002");
        Long spId = createAndGetId("/api/server-profiles", validServerProfileDTO(projectId));

        mockMvc.perform(get("/api/server-profiles/{id}", spId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(spId.intValue()))
                .andExpect(jsonPath("$.data.serverName").value("生产服务器"));
    }

    @Test
    void serverProfile_listByProject() throws Exception {
        Long projectId = createProject("SP-003");
        createAndGetId("/api/server-profiles", validServerProfileDTO(projectId));

        mockMvc.perform(get("/api/server-profiles").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void serverProfile_blankServerName_returns400() throws Exception {
        Long projectId = createProject("SP-V1");
        ServerProfileCreateDTO dto = validServerProfileDTO(projectId);
        dto.setServerName("");

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("服务器名称不能为空")));
    }

    @Test
    void serverProfile_blankHost_returns400() throws Exception {
        Long projectId = createProject("SP-V2");
        ServerProfileCreateDTO dto = validServerProfileDTO(projectId);
        dto.setHost("");

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("主机地址不能为空")));
    }

    @Test
    void serverProfile_portOutOfRange_returns400() throws Exception {
        Long projectId = createProject("SP-V3");
        ServerProfileCreateDTO dto = validServerProfileDTO(projectId);
        dto.setPort(70000);

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("端口号不能大于65535")));
    }

    @Test
    void serverProfile_blankDbName_returns400() throws Exception {
        Long projectId = createProject("SP-V4");
        ServerProfileCreateDTO dto = validServerProfileDTO(projectId);
        dto.setDbName("");

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("数据库名称不能为空")));
    }

    @Test
    void serverProfile_healthCheck() throws Exception {
        Long projectId = createProject("SP-HC");
        Long spId = createAndGetId("/api/server-profiles", validServerProfileDTO(projectId));

        mockMvc.perform(post("/api/server-profiles/{id}/health-check", spId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(spId.intValue()))
                .andExpect(jsonPath("$.data.lastHealthCheck").isNotEmpty())
                .andExpect(jsonPath("$.data.networkLatencyMs").isNumber())
                .andExpect(jsonPath("$.data.status", anyOf(is("CONNECTED"), is("DISCONNECTED"))));
    }

    @Test
    void serverProfile_duplicateHostPortDb_returns400() throws Exception {
        Long projectId = createProject("SP-DUP");
        createAndGetId("/api/server-profiles", validServerProfileDTO(projectId));

        Long projectId2 = createProject("SP-DUP2");
        ServerProfileCreateDTO dto2 = validServerProfileDTO(projectId2);
        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("相同主机、端口、数据库名称的服务器配置已存在")));
    }

    @Test
    void serverProfile_invalidProject_returns400() throws Exception {
        ServerProfileCreateDTO dto = validServerProfileDTO(999999L);

        mockMvc.perform(post("/api/server-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ===================== GoLiveCheck Tests =====================

    @Test
    void goLiveCheck_initCreatesItems() throws Exception {
        Long projectId = createProject("GL-001");

        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(15)))
                .andExpect(jsonPath("$.data[0].projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data[0].checkResult").value("UNCHECKED"))
                .andExpect(jsonPath("$.data[0].itemName").isNotEmpty());
    }

    @Test
    void goLiveCheck_listByProject() throws Exception {
        Long projectId = createProject("GL-002");

        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/go-live-checks").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(15)));
    }

    @Test
    void goLiveCheck_updateItem() throws Exception {
        Long projectId = createProject("GL-003");

        MvcResult initResult = mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(initResult.getResponse().getContentAsString()).path("data");
        Long itemId = items.get(0).path("id").asLong();

        GoLiveCheckUpdateDTO updateDto = new GoLiveCheckUpdateDTO();
        updateDto.setCheckResult("PASS");
        updateDto.setDetail("数据完整");
        updateDto.setCheckedBy("QA李");

        mockMvc.perform(put("/api/go-live-checks/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkResult").value("PASS"))
                .andExpect(jsonPath("$.data.detail").value("数据完整"))
                .andExpect(jsonPath("$.data.checkedBy").value("QA李"))
                .andExpect(jsonPath("$.data.checkedAt").isNotEmpty());
    }

    @Test
    void goLiveCheck_readinessNotReady() throws Exception {
        Long projectId = createProject("GL-004");

        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/go-live-checks/readiness/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(15))
                .andExpect(jsonPath("$.data.unchecked").value(15))
                .andExpect(jsonPath("$.data.ready").value(false))
                .andExpect(jsonPath("$.data.message", containsString("不允许上线")));
    }

    @Test
    void goLiveCheck_readinessReady() throws Exception {
        Long projectId = createProject("GL-005");

        MvcResult initResult = mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(initResult.getResponse().getContentAsString()).path("data");

        for (int i = 0; i < items.size(); i++) {
            Long itemId = items.get(i).path("id").asLong();
            GoLiveCheckUpdateDTO dto = new GoLiveCheckUpdateDTO();
            dto.setCheckResult("PASS");
            dto.setDetail("已验证");
            dto.setCheckedBy("QA");
            mockMvc.perform(put("/api/go-live-checks/{id}", itemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(dto)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/go-live-checks/readiness/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(15))
                .andExpect(jsonPath("$.data.passed").value(15))
                .andExpect(jsonPath("$.data.ready").value(true))
                .andExpect(jsonPath("$.data.message", containsString("允许上线")));
    }

    @Test
    void goLiveCheck_blankCheckResult_returns400() throws Exception {
        Long projectId = createProject("GL-V1");

        MvcResult initResult = mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        Long itemId = objectMapper.readTree(initResult.getResponse().getContentAsString())
                .path("data").get(0).path("id").asLong();

        GoLiveCheckUpdateDTO dto = new GoLiveCheckUpdateDTO();
        dto.setCheckResult("");

        mockMvc.perform(put("/api/go-live-checks/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("检查结果不能为空")));
    }

    @Test
    void goLiveCheck_invalidCheckResult_returns400() throws Exception {
        Long projectId = createProject("GL-V2");

        MvcResult initResult = mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        Long itemId = objectMapper.readTree(initResult.getResponse().getContentAsString())
                .path("data").get(0).path("id").asLong();

        GoLiveCheckUpdateDTO dto = new GoLiveCheckUpdateDTO();
        dto.setCheckResult("INVALID");

        mockMvc.perform(put("/api/go-live-checks/{id}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效的检查结果")));
    }

    @Test
    void goLiveCheck_duplicateInit_returns400() throws Exception {
        Long projectId = createProject("GL-DUP");

        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目已初始化上线检查清单")));
    }

    @Test
    void goLiveCheck_initInvalidProject_returns400() throws Exception {
        mockMvc.perform(post("/api/go-live-checks/init/{projectId}", 999999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    @Test
    void goLiveCheck_readinessWithoutInit_returns400() throws Exception {
        Long projectId = createProject("GL-NOINIT");

        mockMvc.perform(get("/api/go-live-checks/readiness/{projectId}", projectId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目尚未初始化上线检查清单")));
    }

    // ===================== SimulationScene Tests =====================

    @Test
    void simulationScene_happyPath() throws Exception {
        Long projectId = createProject("SS-001");
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(projectId);

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.sceneName").value("采购入库流程"))
                .andExpect(jsonPath("$.data.sceneType").value("PURCHASE_INBOUND"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.steps").isNotEmpty());
    }

    @Test
    void simulationScene_listByProject() throws Exception {
        Long projectId = createProject("SS-002");
        createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationSceneCreateDTO dto2 = validSimulationSceneDTO(projectId);
        dto2.setSceneName("销售出库流程");
        dto2.setSceneType("SALES_OUTBOUND");
        createAndGetId("/api/simulation-scenes", dto2);

        mockMvc.perform(get("/api/simulation-scenes").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void simulationScene_execute() throws Exception {
        Long projectId = createProject("SS-003");
        Long sceneId = createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("流程完成，数据正确");
        execDto.setStatus("PASSED");
        execDto.setExecutedBy("QA王");
        execDto.setDeviation("无偏差");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.actualResult").value("流程完成，数据正确"))
                .andExpect(jsonPath("$.data.executedBy").value("QA王"))
                .andExpect(jsonPath("$.data.executedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.deviation").value("无偏差"));
    }

    @Test
    void simulationScene_executeFailed() throws Exception {
        Long projectId = createProject("SS-004");
        Long sceneId = createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("结算金额不一致");
        execDto.setStatus("FAILED");
        execDto.setExecutedBy("QA赵");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"));
    }

    @Test
    void simulationScene_blankSceneName_returns400() throws Exception {
        Long projectId = createProject("SS-V1");
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(projectId);
        dto.setSceneName("");

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("场景名称不能为空")));
    }

    @Test
    void simulationScene_blankSceneType_returns400() throws Exception {
        Long projectId = createProject("SS-V2");
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(projectId);
        dto.setSceneType("");

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("场景类型不能为空")));
    }

    @Test
    void simulationScene_blankSteps_returns400() throws Exception {
        Long projectId = createProject("SS-V3");
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(projectId);
        dto.setSteps("");

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("操作步骤不能为空")));
    }

    @Test
    void simulationScene_invalidSceneType_returns400() throws Exception {
        Long projectId = createProject("SS-V4");
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(projectId);
        dto.setSceneType("INVALID_TYPE");

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效的场景类型")));
    }

    @Test
    void simulationScene_duplicateNameInProject_returns400() throws Exception {
        Long projectId = createProject("SS-DUP");
        createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validSimulationSceneDTO(projectId))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("该项目下场景名称已存在")));
    }

    @Test
    void simulationScene_invalidProject_returns400() throws Exception {
        SimulationSceneCreateDTO dto = validSimulationSceneDTO(999999L);

        mockMvc.perform(post("/api/simulation-scenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    @Test
    void simulationScene_executeBlankActualResult_returns400() throws Exception {
        Long projectId = createProject("SS-EV1");
        Long sceneId = createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("");
        execDto.setStatus("PASSED");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("实际结果不能为空")));
    }

    @Test
    void simulationScene_executeBlankStatus_returns400() throws Exception {
        Long projectId = createProject("SS-EV2");
        Long sceneId = createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("某结果");
        execDto.setStatus("");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("执行状态不能为空")));
    }

    @Test
    void simulationScene_executeInvalidStatus_returns400() throws Exception {
        Long projectId = createProject("SS-EV3");
        Long sceneId = createAndGetId("/api/simulation-scenes", validSimulationSceneDTO(projectId));

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("某结果");
        execDto.setStatus("INVALID");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("无效的执行状态")));
    }

    @Test
    void simulationScene_executeNonExistentScene_returns400() throws Exception {
        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("结果");
        execDto.setStatus("PASSED");

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("模拟场景不存在")));
    }
}
