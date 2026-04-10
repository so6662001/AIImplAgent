package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.research.dto.CustomerProfileCreateDTO;
import com.aimpl.domain.simulation.dto.SimulationExecuteDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SimulationAgentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

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
        dto.setRemark("模拟演练测试");

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createProfile(Long projectId) throws Exception {
        CustomerProfileCreateDTO dto = new CustomerProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setCompanyName("测试钢贸公司");
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setTotalStaff(200);
        dto.setTotalProductionLines(3);
        dto.setMonthlyVolume(new BigDecimal("5000"));
        dto.setMonthlyAmount(new BigDecimal("20000000"));
        dto.setTotalWarehouseCount(3);
        dto.setManagementGoals("提升管理效率");

        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createMinimalProfile(Long projectId) throws Exception {
        CustomerProfileCreateDTO dto = new CustomerProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setCompanyName("最小画像公司");
        dto.setManagementGoals("基本管理");

        MvcResult result = mockMvc.perform(post("/api/customer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private void autoGenerateScenes(Long projectId) throws Exception {
        mockMvc.perform(post("/api/simulation/scenes/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk());
    }

    private void executeSceneByIndex(Long projectId, int index, String status) throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/simulation-scenes")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode scenes = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .path("data");
        long sceneId = scenes.get(index).path("id").asLong();

        SimulationExecuteDTO execDto = new SimulationExecuteDTO();
        execDto.setActualResult("测试执行结果");
        execDto.setStatus(status);
        execDto.setExecutedBy("测试员");
        if ("FAILED".equals(status)) {
            execDto.setDeviation("存在偏差");
        }

        mockMvc.perform(put("/api/simulation-scenes/{id}/execute", sceneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(execDto)))
                .andExpect(status().isOk());
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    // ========================= Test 1: Mock data — lists not empty =========================

    @Test
    void generateMockData_listsNotEmpty() throws Exception {
        Long projectId = createProject("SIM-MOCK-1");
        createProfile(projectId);

        mockMvc.perform(post("/api/simulation/mock-data/generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.data.products", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.customers").isArray())
                .andExpect(jsonPath("$.data.customers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.suppliers").isArray())
                .andExpect(jsonPath("$.data.suppliers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.inventory").isArray())
                .andExpect(jsonPath("$.data.inventory", hasSize(greaterThan(0))));
    }

    // ========================= Test 2: Mock data — steel-related product names =========================

    @Test
    void generateMockData_productsHaveSteelRelatedNames() throws Exception {
        Long projectId = createProject("SIM-MOCK-2");
        createProfile(projectId);

        mockMvc.perform(post("/api/simulation/mock-data/generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.products[0].name").isString())
                .andExpect(jsonPath("$.data.products[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data.products[0].material").isString())
                .andExpect(jsonPath("$.data.products[0].material").isNotEmpty())
                .andExpect(jsonPath("$.data.products[0].unit").isNotEmpty());
    }

    // ========================= Test 3: Mock data — minimal profile uses defaults =========================

    @Test
    void generateMockData_minimalProfileUsesDefaults() throws Exception {
        Long projectId = createProject("SIM-MOCK-3");
        createMinimalProfile(projectId);

        mockMvc.perform(post("/api/simulation/mock-data/generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.products", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.customers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.suppliers", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.inventory", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.summary.totalProducts", greaterThan(0)));
    }

    // ========================= Test 4: Auto-generate scenes — >= 5 scenes =========================

    @Test
    void autoGenerateScenes_createsAtLeast5Scenes() throws Exception {
        Long projectId = createProject("SIM-SCENE-1");

        mockMvc.perform(post("/api/simulation/scenes/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(5))));
    }

    // ========================= Test 5: Auto-generate scenes — scenes have steps =========================

    @Test
    void autoGenerateScenes_scenesHaveSteps() throws Exception {
        Long projectId = createProject("SIM-SCENE-2");

        MvcResult result = mockMvc.perform(post("/api/simulation/scenes/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode scenes = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        for (JsonNode scene : scenes) {
            JsonNode steps = scene.path("steps");
            assertTrue(steps.isArray() && steps.size() > 0,
                    "场景 \"" + scene.path("sceneName").asText() + "\" 应包含步骤");
        }
    }

    // ========================= Test 6: Auto-generate scenes — no duplicates on second call =========================

    @Test
    void autoGenerateScenes_noDuplicatesOnSecondCall() throws Exception {
        Long projectId = createProject("SIM-SCENE-3");

        MvcResult first = mockMvc.perform(post("/api/simulation/scenes/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode firstScenes = objectMapper.readTree(first.getResponse().getContentAsString()).path("data");
        int firstCount = firstScenes.size();

        mockMvc.perform(post("/api/simulation/scenes/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(get("/api/simulation-scenes")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode allScenes = objectMapper.readTree(listResult.getResponse().getContentAsString()).path("data");

        assertEquals(firstCount, allScenes.size(),
                "第二次调用不应增加重复场景");

        Set<String> names = new HashSet<>();
        for (JsonNode s : allScenes) {
            String name = s.path("sceneName").asText();
            assertTrue(names.add(name), "发现重复场景名称: " + name);
        }
    }

    // ========================= Test 7: Report — all PENDING → 0% pass rate =========================

    @Test
    void evaluationReport_allPending_zeroPassRate() throws Exception {
        Long projectId = createProject("SIM-RPT-1");
        autoGenerateScenes(projectId);

        mockMvc.perform(get("/api/simulation/reports/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overallPassRate").value(0))
                .andExpect(jsonPath("$.data.readyForTraining").value(false))
                .andExpect(jsonPath("$.data.executedScenes").value(0));
    }

    // ========================= Test 8: Report — execute some PASSED → pass rate calculated =========================

    @Test
    void evaluationReport_partialExecution_passRateCalculated() throws Exception {
        Long projectId = createProject("SIM-RPT-2");
        autoGenerateScenes(projectId);

        executeSceneByIndex(projectId, 0, "PASSED");
        executeSceneByIndex(projectId, 1, "FAILED");

        MvcResult result = mockMvc.perform(get("/api/simulation/reports/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.executedScenes").value(2))
                .andExpect(jsonPath("$.data.passedScenes").value(1))
                .andExpect(jsonPath("$.data.failedScenes").value(1))
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        BigDecimal passRate = new BigDecimal(data.path("overallPassRate").asText());
        assertTrue(passRate.compareTo(BigDecimal.ZERO) > 0, "通过率应大于0");
        assertTrue(passRate.compareTo(new BigDecimal("100")) < 0, "通过率应小于100");
    }

    // ========================= Test 9: Report — readyForTraining when passRate >= 80% =========================

    @Test
    void evaluationReport_highPassRate_readyForTraining() throws Exception {
        Long projectId = createProject("SIM-RPT-3");
        autoGenerateScenes(projectId);

        MvcResult listResult = mockMvc.perform(get("/api/simulation-scenes")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode scenes = objectMapper.readTree(listResult.getResponse().getContentAsString()).path("data");
        int totalScenes = scenes.size();

        for (int i = 0; i < totalScenes; i++) {
            executeSceneByIndex(projectId, i, "PASSED");
        }

        mockMvc.perform(get("/api/simulation/reports/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallPassRate").value(100.00))
                .andExpect(jsonPath("$.data.readyForTraining").value(true));
    }

    // ========================= Test 10: Report — recommendations not empty when failures =========================

    @Test
    void evaluationReport_withFailures_hasRecommendations() throws Exception {
        Long projectId = createProject("SIM-RPT-4");
        autoGenerateScenes(projectId);

        executeSceneByIndex(projectId, 0, "FAILED");

        mockMvc.perform(get("/api/simulation/reports/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.failedScenes").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.recommendations", hasSize(greaterThan(0))));
    }
}
