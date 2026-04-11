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
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SharedLayerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
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
        dto.setRemark("共享层集成测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private void createLlmProviderAndAgent(String agentCode) throws Exception {
        Map<String, Object> provider = Map.of(
                "providerName", "TestProvider_" + agentCode,
                "providerType", "OPENAI",
                "apiEndpoint", "https://api.test.example.com/v1",
                "apiKey", "sk-test-key-12345",
                "modelName", "gpt-4-test",
                "maxTokens", 4096,
                "temperature", 0.7,
                "enabled", true
        );
        MvcResult providerResult = mockMvc.perform(post("/api/llm-providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(provider)))
                .andExpect(status().isOk())
                .andReturn();
        Long providerId = extractId(providerResult);

        Map<String, Object> agent = Map.of(
                "agentCode", agentCode,
                "agentName", "测试智能体_" + agentCode,
                "description", "集成测试用智能体",
                "llmProviderId", providerId,
                "promptTemplate", "你是一个{{agentCode}}助手。",
                "ragEnabled", false,
                "ragCollectionName", "",
                "enabled", true
        );
        mockMvc.perform(post("/api/agent-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(agent)))
                .andExpect(status().isOk());
    }

    // =================== LLM Gateway Tests ===================

    @Test
    void llmGateway_completeWithValidAgent_responseNotEmpty() throws Exception {
        createLlmProviderAndAgent("SHARED_TEST_A");

        Map<String, Object> request = Map.of(
                "agentCode", "SHARED_TEST_A",
                "prompt", "请生成一份测试报告"
        );
        mockMvc.perform(post("/api/llm-gateway/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.response", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.providerName").value("TestProvider_SHARED_TEST_A"))
                .andExpect(jsonPath("$.data.modelName").value("gpt-4-test"))
                .andExpect(jsonPath("$.data.tokensUsed", greaterThan(0)))
                .andExpect(jsonPath("$.data.executionTimeMs", greaterThanOrEqualTo(0)));
    }

    @Test
    void llmGateway_invalidAgentCode_returns400() throws Exception {
        Map<String, Object> request = Map.of(
                "agentCode", "NON_EXISTENT_AGENT_XYZ",
                "prompt", "测试请求"
        );
        mockMvc.perform(post("/api/llm-gateway/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("智能体配置不存在")));
    }

    @Test
    void llmGateway_testEndpoint_returnsMockResponse() throws Exception {
        createLlmProviderAndAgent("SHARED_TEST_B");

        mockMvc.perform(get("/api/llm-gateway/test/{agentCode}", "SHARED_TEST_B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.agentCode").value("SHARED_TEST_B"))
                .andExpect(jsonPath("$.data.response", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.fallbackUsed").value(false));
    }

    @Test
    void llmGateway_logsDecision_verifyDecisionLogged() throws Exception {
        createLlmProviderAndAgent("SHARED_TEST_C");

        Map<String, Object> request = Map.of(
                "agentCode", "SHARED_TEST_C",
                "prompt", "决策日志验证请求"
        );
        mockMvc.perform(post("/api/llm-gateway/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.decisionLogId", notNullValue()))
                .andExpect(jsonPath("$.data.decisionLogId", greaterThan(0)));

        mockMvc.perform(get("/api/agent-decisions")
                        .param("agentCode", "SHARED_TEST_C"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].agentCode").value("SHARED_TEST_C"))
                .andExpect(jsonPath("$.data[0].triggerType").value("LLM_CALL"))
                .andExpect(jsonPath("$.data[0].modelUsed").value("gpt-4-test"));
    }

    // =================== Document Generation Tests ===================

    @Test
    void documentTypes_forProject_returnsFourTypes() throws Exception {
        Long projectId = createProject("DOC-TYPE-001");

        mockMvc.perform(get("/api/documents/types/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[?(@.typeCode=='RESEARCH_REPORT')]").exists())
                .andExpect(jsonPath("$.data[?(@.typeCode=='DELIVERY_PLAN')]").exists())
                .andExpect(jsonPath("$.data[?(@.typeCode=='SIMULATION_REPORT')]").exists())
                .andExpect(jsonPath("$.data[?(@.typeCode=='DELIVERY_REPORT')]").exists());
    }

    @Test
    void generateDeliveryReport_success() throws Exception {
        Long projectId = createProject("DOC-GEN-001");

        Map<String, Object> request = Map.of(
                "documentType", "DELIVERY_REPORT",
                "projectId", projectId
        );
        mockMvc.perform(post("/api/documents/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.documentType").value("DELIVERY_REPORT"))
                .andExpect(jsonPath("$.data.documentName", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.content", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.status").value("GENERATED"));
    }

    @Test
    void generateInvalidType_returns400() throws Exception {
        Long projectId = createProject("DOC-INV-001");

        Map<String, Object> request = Map.of(
                "documentType", "INVALID_TYPE_XYZ",
                "projectId", projectId
        );
        mockMvc.perform(post("/api/documents/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("不支持的文档类型")));
    }

    // =================== Analytics Tests ===================

    @Test
    void projectAnalytics_healthScoreBetween0And100() throws Exception {
        Long projectId = createProject("ANALYTICS-001");

        mockMvc.perform(get("/api/analytics/project/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.overallHealthScore", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.overallHealthScore", lessThanOrEqualTo(100)))
                .andExpect(jsonPath("$.data.healthLevel", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.trainingMetrics").exists())
                .andExpect(jsonPath("$.data.importMetrics").exists())
                .andExpect(jsonPath("$.data.ticketMetrics").exists())
                .andExpect(jsonPath("$.data.simulationMetrics").exists());
    }

    @Test
    void departmentAnalytics_verifyTotalEngineersCount() throws Exception {
        mockMvc.perform(get("/api/analytics/department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalEngineers", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.activeProjects", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.completedProjects", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.avgUtilizationRate", notNullValue()))
                .andExpect(jsonPath("$.data.ticketResolutionRate", notNullValue()))
                .andExpect(jsonPath("$.data.knowledgeEntryCount", greaterThanOrEqualTo(0)));
    }

    @Test
    void projectAnalytics_newProjectWithNoData_verifyDefaults() throws Exception {
        Long projectId = createProject("ANALYTICS-EMPTY-001");

        mockMvc.perform(get("/api/analytics/project/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.trainingMetrics.traineeCount").value(0))
                .andExpect(jsonPath("$.data.trainingMetrics.passRate").value(0))
                .andExpect(jsonPath("$.data.trainingMetrics.kaReady").value(false))
                .andExpect(jsonPath("$.data.importMetrics.completed").value(0))
                .andExpect(jsonPath("$.data.importMetrics.rate").value(0))
                .andExpect(jsonPath("$.data.ticketMetrics.total").value(0))
                .andExpect(jsonPath("$.data.ticketMetrics.resolved").value(0))
                .andExpect(jsonPath("$.data.simulationMetrics.total").value(0))
                .andExpect(jsonPath("$.data.simulationMetrics.passed").value(0));
    }
}
