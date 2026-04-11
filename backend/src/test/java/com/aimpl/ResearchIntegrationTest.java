package com.aimpl;

import com.aimpl.common.enums.IndustryType;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================= Helpers =========================

    private Long createProject(String code) throws Exception {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale("大型");
        dto.setPmId(1L);
        dto.setModules(List.of("采购", "销售"));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("research integration test");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Map<String, Object> fullProfileMap(Long projectId) {
        Map<String, Object> m = new HashMap<>();
        m.put("projectId", projectId);
        m.put("companyName", "测试钢铁有限公司");
        m.put("industryType", "STEEL_TRADER");
        m.put("businessModel", "STEEL_TRADER");
        m.put("tradeMode", "DIRECT");
        m.put("tradeScope", "DOMESTIC");
        m.put("mainBusiness", "钢材贸易");
        m.put("legalPerson", "张三");
        m.put("registeredCapital", "5000万元");
        m.put("establishmentDate", "2010-01-15");
        m.put("address", "上海市浦东新区");
        m.put("salesMode", "现货批发");
        m.put("totalProductionLines", 2);
        m.put("productionShifts", "两班");
        m.put("mesCurrentStatus", "无");
        m.put("qualityStandards", "ISO9001");
        m.put("totalWarehouseCount", 3);
        m.put("totalWarehouseAreaSqm", 15000.50);
        m.put("totalCraneCount", 5);
        m.put("inventoryTurnoverRate", 6.5);
        m.put("inventoryManagementMethod", "Excel");
        m.put("monthlyVolume", 5000.00);
        m.put("monthlyAmount", 2500.00);
        m.put("pricingModel", "一口价");
        m.put("settlementMethods", "现款现货,账期");
        m.put("creditPolicy", "授信额度100万");
        m.put("totalCustomerCount", 200);
        m.put("customerTypes", "终端用户,贸易商");
        m.put("topCustomers", "客户A,客户B,客户C");
        m.put("totalStaff", 150);
        m.put("departments", "销售部,采购部,仓储部,财务部");
        m.put("keyPositions", "销售经理,采购经理,仓管员");
        m.put("decisionChain", "销售员→销售经理→总经理");
        m.put("existingSystems", "[{\"name\":\"用友U8\",\"modules\":[\"财务\"]}]");
        m.put("targetModules", "进销存,财务,仓储");
        m.put("modulePriorities", "进销存优先,其次财务");
        m.put("managementGoals", "实现全流程信息化管理");
        m.put("processGoals", "优化采购到销售流程");
        m.put("efficiencyGoals", "提升库存周转效率30%");
        m.put("riskControlGoals", "建立应收账款预警机制");
        return m;
    }

    private Long createProfileAndGetId(Long projectId) throws Exception {
        Map<String, Object> profile = fullProfileMap(projectId);
        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    // ========================= 1. Create profile with all fields =========================

    @Test
    void createProfile_allFields_persisted() throws Exception {
        Long projectId = createProject("RES-001");
        Map<String, Object> profile = fullProfileMap(projectId);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.companyName").value("测试钢铁有限公司"))
                .andExpect(jsonPath("$.data.industryType").value("STEEL_TRADER"))
                .andExpect(jsonPath("$.data.legalPerson").value("张三"))
                .andExpect(jsonPath("$.data.registeredCapital").value("5000万元"))
                .andExpect(jsonPath("$.data.address").value("上海市浦东新区"))
                .andExpect(jsonPath("$.data.salesMode").value("现货批发"))
                .andExpect(jsonPath("$.data.totalStaff").value(150))
                .andExpect(jsonPath("$.data.departments").value("销售部,采购部,仓储部,财务部"))
                .andExpect(jsonPath("$.data.managementGoals").value("实现全流程信息化管理"))
                .andExpect(jsonPath("$.data.targetModules").value("进销存,财务,仓储"))
                .andExpect(jsonPath("$.data.existingSystems").value("[{\"name\":\"用友U8\",\"modules\":[\"财务\"]}]"));
    }

    // ========================= 2. Blank companyName → 400 =========================

    @Test
    void createProfile_blankCompanyName_returns400() throws Exception {
        Long projectId = createProject("RES-002");
        Map<String, Object> profile = fullProfileMap(projectId);
        profile.put("companyName", "");

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("公司名称不能为空")));
    }

    // ========================= 3. Invalid projectId → 400 =========================

    @Test
    void createProfile_invalidProjectId_returns400() throws Exception {
        Map<String, Object> profile = fullProfileMap(999999L);

        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= 4. List profiles by projectId =========================

    @Test
    void listProfiles_byProjectId() throws Exception {
        Long projectId = createProject("RES-004");
        Map<String, Object> p1 = fullProfileMap(projectId);
        p1.put("companyName", "公司甲");
        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isOk());

        Map<String, Object> p2 = fullProfileMap(projectId);
        p2.put("companyName", "公司乙");
        mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customer-profiles")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= 5. Questionnaire STEEL_TRADER — 10 sections =========================

    @Test
    void generateQuestionnaire_steelTrader_10sections_productionFewer() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("industryType", "STEEL_TRADER");
        req.put("scale", "中型");
        req.put("modules", List.of("进销存", "财务"));

        MvcResult result = mockMvc.perform(post("/api/research/questionnaire/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sections", hasSize(10)))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        JsonNode sections = data.path("sections");

        // Find production section (index 2, "生产能力")
        JsonNode productionSection = sections.get(2);
        int productionQuestions = productionSection.path("questions").size();
        // STEEL_TRADER: not detailed, should have exactly 3 questions
        assert productionQuestions == 3 : "Expected 3 production questions for STEEL_TRADER, got " + productionQuestions;
    }

    // ========================= 6. Questionnaire STEEL_MILL — production ≥ 6 =========================

    @Test
    void generateQuestionnaire_steelMill_productionHasMoreQuestions() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("industryType", "STEEL_MILL");
        req.put("scale", "大型");
        req.put("modules", List.of("进销存", "生产"));

        MvcResult result = mockMvc.perform(post("/api/research/questionnaire/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sections", hasSize(10)))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        JsonNode sections = data.path("sections");
        JsonNode productionSection = sections.get(2);
        int productionQuestions = productionSection.path("questions").size();
        assert productionQuestions >= 6 : "Expected ≥6 production questions for STEEL_MILL, got " + productionQuestions;
    }

    // ========================= 7. Questionnaire blank industryType → 400 =========================

    @Test
    void generateQuestionnaire_blankIndustryType_returns400() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("industryType", "");
        req.put("scale", "中型");
        req.put("modules", List.of("进销存"));

        mockMvc.perform(post("/api/research/questionnaire/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("行业类型不能为空")));
    }

    // ========================= 8. Generate report — 6 sections =========================

    @Test
    void generateReport_returns6Sections() throws Exception {
        Long projectId = createProject("RES-008");
        Long profileId = createProfileAndGetId(projectId);

        mockMvc.perform(post("/api/research/reports/generate/{profileId}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.profileId").value(profileId.intValue()))
                .andExpect(jsonPath("$.data.companyName").value("测试钢铁有限公司"))
                .andExpect(jsonPath("$.data.sections", hasSize(6)))
                .andExpect(jsonPath("$.data.overallRiskLevel").isString());
    }

    // ========================= 9. Generate report non-existent profileId → 400 =========================

    @Test
    void generateReport_nonExistentProfileId_returns400() throws Exception {
        mockMvc.perform(post("/api/research/reports/generate/{profileId}", 999999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("客户画像不存在")));
    }

    // ========================= 10. List reports by projectId =========================

    @Test
    void listReports_byProjectId() throws Exception {
        Long projectId = createProject("RES-010");
        Long profileId = createProfileAndGetId(projectId);

        mockMvc.perform(post("/api/research/reports/generate/{profileId}", profileId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/research/reports")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].profileId").value(profileId.intValue()))
                .andExpect(jsonPath("$.data[0].overallRiskLevel").isString());
    }

    // ========================= 11. Risk level: large staff + many systems → higher risk =========================

    @Test
    void reportRiskLevel_largeStaffAndManySystems_higherRisk() throws Exception {
        Long projectId = createProject("RES-011");
        Map<String, Object> profile = fullProfileMap(projectId);
        profile.put("companyName", "大型高风险公司");
        profile.put("totalStaff", 800);
        profile.put("existingSystems", "[{\"name\":\"SAP\"},{\"name\":\"用友\"},{\"name\":\"金蝶\"}]");
        profile.put("totalProductionLines", 5);
        profile.put("totalWarehouseCount", 8);
        profile.put("decisionChain", "操作员→主管→部门经理→副总→总经理→董事会");

        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andReturn();
        Long profileId = extractId(result);

        MvcResult reportResult = mockMvc.perform(post("/api/research/reports/generate/{profileId}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode data = objectMapper.readTree(reportResult.getResponse().getContentAsString()).path("data");
        String riskLevel = data.path("overallRiskLevel").asText();
        // totalStaff>500 (+2) + existingSystems(+2) + productionLines>3(+2) + warehouses>5(+1) + decisionChain>20(+1) = 8 → HIGH
        assert "HIGH".equals(riskLevel) : "Expected HIGH risk, got " + riskLevel;
    }

    // ========================= 12. Production fields persisted =========================

    @Test
    void profileProductionFields_persisted() throws Exception {
        Long projectId = createProject("RES-012");
        Map<String, Object> profile = fullProfileMap(projectId);
        profile.put("companyName", "生产测试公司");
        profile.put("totalProductionLines", 8);
        profile.put("productionShifts", "三班");
        profile.put("mesCurrentStatus", "部分上线");
        profile.put("qualityStandards", "ISO9001,IATF16949");

        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = extractId(result);

        mockMvc.perform(get("/api/customer-profiles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalProductionLines").value(8))
                .andExpect(jsonPath("$.data.productionShifts").value("三班"))
                .andExpect(jsonPath("$.data.mesCurrentStatus").value("部分上线"))
                .andExpect(jsonPath("$.data.qualityStandards").value("ISO9001,IATF16949"));
    }

    // ========================= 13. Inventory fields persisted =========================

    @Test
    void profileInventoryFields_persisted() throws Exception {
        Long projectId = createProject("RES-013");
        Map<String, Object> profile = fullProfileMap(projectId);
        profile.put("companyName", "仓储测试公司");
        profile.put("totalWarehouseAreaSqm", 25000.75);
        profile.put("totalCraneCount", 12);
        profile.put("inventoryTurnoverRate", 8.3);
        profile.put("inventoryManagementMethod", "WMS系统");

        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = extractId(result);

        mockMvc.perform(get("/api/customer-profiles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalWarehouseAreaSqm").value(25000.75))
                .andExpect(jsonPath("$.data.totalCraneCount").value(12))
                .andExpect(jsonPath("$.data.inventoryTurnoverRate").value(8.3))
                .andExpect(jsonPath("$.data.inventoryManagementMethod").value("WMS系统"));
    }
}
