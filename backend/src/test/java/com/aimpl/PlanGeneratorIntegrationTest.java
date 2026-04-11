package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.project.dto.PlanGenerateRequestDTO;
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

import java.nio.charset.StandardCharsets;
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
class PlanGeneratorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private String responseBody(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private Long createProject(String code, String scale, String modules) throws Exception {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale(scale);
        dto.setPmId(1L);
        dto.setModules(List.of(modules.split(",")));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2025, 6, 1));
        dto.setRemark("计划生成测试");

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(responseBody(result));
        return root.path("data").path("id").asLong();
    }

    private JsonNode generateAndGetData(PlanGenerateRequestDTO request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/project-plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return objectMapper.readTree(responseBody(result)).path("data");
    }

    // Test 1: Generate plan for 大型 project — verify estimatedDuration ≥ 90
    @Test
    void generate_largeProject_durationAtLeast90() throws Exception {
        Long projectId = createProject("PG-LARGE", "大型", "采购,销售,库存");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        int duration = data.path("estimatedDurationDays").asInt();
        assert duration >= 90 : "Large project duration should be >= 90, got " + duration;
    }

    // Test 2: Generate plan for 小型 project — verify estimatedDuration ≤ 45
    @Test
    void generate_smallProject_durationAtMost45() throws Exception {
        Long projectId = createProject("PG-SMALL", "小型", "采购");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        int duration = data.path("estimatedDurationDays").asInt();
        assert duration <= 45 : "Small project duration should be <= 45, got " + duration;
    }

    // Test 3: Verify 7+ milestones generated with dayOffsets in ascending order
    @Test
    void generate_milestonesAtLeast7_ascendingDayOffsets() throws Exception {
        Long projectId = createProject("PG-MILE", "中型", "采购,销售");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        JsonNode milestones = data.path("milestones");
        assert milestones.size() >= 7 : "Should have >= 7 milestones, got " + milestones.size();

        int prev = -1;
        for (int i = 0; i < milestones.size(); i++) {
            int offset = milestones.get(i).path("dayOffset").asInt();
            assert offset >= prev : "Milestone dayOffsets not ascending: " + prev + " -> " + offset + " at index " + i;
            prev = offset;
        }
    }

    // Test 4: Verify WBS items cover all phases
    @Test
    void generate_wbsCoversAllPhases() throws Exception {
        Long projectId = createProject("PG-WBS", "中型", "采购,销售");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        JsonNode wbsItems = data.path("wbsItems");

        java.util.Set<String> phases = new java.util.HashSet<>();
        for (int i = 0; i < wbsItems.size(); i++) {
            phases.add(wbsItems.get(i).path("phase").asText());
        }

        List<String> requiredPhases = List.of("调研", "计划", "演练", "培训", "数据", "上线", "跟进", "验收");
        for (String phase : requiredPhases) {
            assert phases.contains(phase) : "WBS missing phase: " + phase + ", found: " + phases;
        }
    }

    // Test 5: Verify resource plan has PM role
    @Test
    void generate_resourcePlanHasPM() throws Exception {
        Long projectId = createProject("PG-RES", "中型", "采购");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        JsonNode resources = data.path("resourcePlan");

        boolean hasPm = false;
        for (int i = 0; i < resources.size(); i++) {
            if ("PM".equals(resources.get(i).path("role").asText())) {
                hasPm = true;
                break;
            }
        }
        assert hasPm : "Resource plan should include PM role";
    }

    // Test 6: Verify risk plan includes standard risks
    @Test
    void generate_riskPlanIncludesStandardRisks() throws Exception {
        Long projectId = createProject("PG-RISK", "中型", "采购");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        JsonNode risks = data.path("riskPlan");

        java.util.Set<String> riskNames = new java.util.HashSet<>();
        for (int i = 0; i < risks.size(); i++) {
            riskNames.add(risks.get(i).path("riskName").asText());
        }

        assert riskNames.contains("客户方数据准备延迟") : "Should include '客户方数据准备延迟'";
        assert riskNames.contains("关键用户培训效果不达标") : "Should include '关键用户培训效果不达标'";
        assert riskNames.contains("系统上线初期操作不熟练") : "Should include '系统上线初期操作不熟练'";
    }

    // Test 7: Verify MES module adds extra duration and MES-specific risk
    @Test
    void generate_mesModule_addsDurationAndRisk() throws Exception {
        Long withMesId = createProject("PG-MES-Y", "中型", "采购,MES");
        Long noMesId = createProject("PG-MES-N", "中型", "采购,销售");

        PlanGenerateRequestDTO reqMes = new PlanGenerateRequestDTO();
        reqMes.setProjectId(withMesId);

        PlanGenerateRequestDTO reqNoMes = new PlanGenerateRequestDTO();
        reqNoMes.setProjectId(noMesId);

        JsonNode dataMes = generateAndGetData(reqMes);
        JsonNode dataNoMes = generateAndGetData(reqNoMes);

        int mesDuration = dataMes.path("estimatedDurationDays").asInt();
        int noMesDuration = dataNoMes.path("estimatedDurationDays").asInt();
        assert mesDuration > noMesDuration :
                "MES duration (" + mesDuration + ") should be > non-MES (" + noMesDuration + ")";

        JsonNode mesRisks = dataMes.path("riskPlan");
        boolean hasMesRisk = false;
        for (int i = 0; i < mesRisks.size(); i++) {
            if (mesRisks.get(i).path("riskName").asText().contains("MES")) {
                hasMesRisk = true;
                break;
            }
        }
        assert hasMesRisk : "MES project should have MES-specific risk";
    }

    // Test 8: Verify 深度定制 adds extra duration and customization risk
    @Test
    void generate_deepCustomization_addsDurationAndRisk() throws Exception {
        Long projectId = createProject("PG-DEEP", "中型", "采购");

        PlanGenerateRequestDTO reqDeep = new PlanGenerateRequestDTO();
        reqDeep.setProjectId(projectId);
        reqDeep.setCustomizationLevel("深度定制");

        Long projectId2 = createProject("PG-STD", "中型", "采购");
        PlanGenerateRequestDTO reqStd = new PlanGenerateRequestDTO();
        reqStd.setProjectId(projectId2);
        reqStd.setCustomizationLevel("标准");

        JsonNode dataDeep = generateAndGetData(reqDeep);
        JsonNode dataStd = generateAndGetData(reqStd);

        int deepDuration = dataDeep.path("estimatedDurationDays").asInt();
        int stdDuration = dataStd.path("estimatedDurationDays").asInt();
        assert deepDuration > stdDuration :
                "Deep customization duration (" + deepDuration + ") should be > standard (" + stdDuration + ")";

        JsonNode deepRisks = dataDeep.path("riskPlan");
        boolean hasCustomRisk = false;
        for (int i = 0; i < deepRisks.size(); i++) {
            if (deepRisks.get(i).path("riskName").asText().contains("定制")) {
                hasCustomRisk = true;
                break;
            }
        }
        assert hasCustomRisk : "Deep customization should have customization-related risk";
    }

    // Test 9: Verify tight deadline triggers HIGH risk
    @Test
    void generate_tightDeadline_triggersHighRisk() throws Exception {
        Long projectId = createProject("PG-DEAD", "大型", "采购,销售,库存");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);
        req.setDeadline(LocalDate.now().plusDays(10));

        JsonNode data = generateAndGetData(req);

        JsonNode risks = data.path("riskPlan");
        boolean hasDeadlineRisk = false;
        for (int i = 0; i < risks.size(); i++) {
            JsonNode risk = risks.get(i);
            if (risk.path("riskName").asText().contains("工期") && "HIGH".equals(risk.path("riskLevel").asText())) {
                hasDeadlineRisk = true;
                break;
            }
        }
        assert hasDeadlineRisk : "Tight deadline should trigger HIGH risk about '工期紧张'";

        String overallRisk = data.path("riskLevel").asText();
        assert "HIGH".equals(overallRisk) : "Overall risk should be HIGH, got " + overallRisk;
    }

    // Test 10: Verify Gantt data generated with same count as WBS
    @Test
    void generate_ganttDataMatchesWbsCount() throws Exception {
        Long projectId = createProject("PG-GANTT", "中型", "采购,销售");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        JsonNode data = generateAndGetData(req);
        int wbsCount = data.path("wbsItems").size();
        int ganttCount = data.path("ganttData").size();
        assert wbsCount == ganttCount :
                "Gantt count (" + ganttCount + ") should match WBS count (" + wbsCount + ")";
        assert ganttCount > 0 : "Gantt data should not be empty";
    }

    // Test 11: Verify plan is persisted (autoGenerated=true)
    @Test
    void generate_planIsPersisted_autoGenerated() throws Exception {
        Long projectId = createProject("PG-PERSIST", "中型", "采购");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        mockMvc.perform(post("/api/project-plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/project-plans")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        JsonNode plans = objectMapper.readTree(responseBody(listResult)).path("data");
        boolean foundAuto = false;
        for (int i = 0; i < plans.size(); i++) {
            if (plans.get(i).path("autoGenerated").asBoolean(false)) {
                foundAuto = true;
                break;
            }
        }
        assert foundAuto : "Should have a persisted plan with autoGenerated=true";
    }

    // Test 12: Get plan detail — verify full VO deserialized
    @Test
    void getDetail_afterGenerate_returnsFullVO() throws Exception {
        Long projectId = createProject("PG-DETAIL", "中型", "采购,销售");

        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(projectId);

        mockMvc.perform(post("/api/project-plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/project-plans")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode plans = objectMapper.readTree(responseBody(listResult)).path("data");
        long planId = plans.get(0).path("id").asLong();

        mockMvc.perform(get("/api/project-plans/" + planId + "/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.estimatedDurationDays").isNumber())
                .andExpect(jsonPath("$.data.riskLevel").isString())
                .andExpect(jsonPath("$.data.milestones").isArray())
                .andExpect(jsonPath("$.data.milestones", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$.data.wbsItems").isArray())
                .andExpect(jsonPath("$.data.resourcePlan").isArray())
                .andExpect(jsonPath("$.data.riskPlan").isArray())
                .andExpect(jsonPath("$.data.ganttData").isArray());
    }

    // Test 13: Generate with invalid projectId → 400
    @Test
    void generate_invalidProjectId_returns400() throws Exception {
        PlanGenerateRequestDTO req = new PlanGenerateRequestDTO();
        req.setProjectId(999999L);

        mockMvc.perform(post("/api/project-plans/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }
}
