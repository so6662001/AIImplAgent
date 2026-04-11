package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.aftersales.dto.AfterSalesTicketCreateDTO;
import com.aimpl.domain.knowledge.dto.KnowledgeEntryCreateDTO;
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
class KnowledgeBaseIntegrationTest {

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

    private KnowledgeEntryCreateDTO knowledgeDTO(String category, String layer,
                                                  String title, String content,
                                                  String keywords) {
        KnowledgeEntryCreateDTO dto = new KnowledgeEntryCreateDTO();
        dto.setCategory(category);
        dto.setLayer(layer);
        dto.setTitle(title);
        dto.setContent(content);
        dto.setKeywords(keywords);
        dto.setSource("集成测试");
        dto.setAccessLevel("PUBLIC");
        return dto;
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
        dto.setRemark("知识库集成测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    // ========================= Knowledge Tests =========================

    @Test
    void createKnowledgeEntry_verifyFields() throws Exception {
        KnowledgeEntryCreateDTO dto = knowledgeDTO(
                "GENERAL", "FAQ", "测试知识标题", "测试知识内容详情", "测试,知识");

        mockMvc.perform(post("/api/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.category").value("GENERAL"))
                .andExpect(jsonPath("$.data.layer").value("FAQ"))
                .andExpect(jsonPath("$.data.title").value("测试知识标题"))
                .andExpect(jsonPath("$.data.content").value("测试知识内容详情"))
                .andExpect(jsonPath("$.data.keywords").value("测试,知识"))
                .andExpect(jsonPath("$.data.viewCount").value(0))
                .andExpect(jsonPath("$.data.helpfulCount").value(0))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void createProjectPrivate_withoutProjectId_returns400() throws Exception {
        KnowledgeEntryCreateDTO dto = knowledgeDTO(
                "PROJECT_PRIVATE", "项目案例", "私有知识", "私有知识内容", "私有");

        mockMvc.perform(post("/api/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("projectId")));
    }

    @Test
    void searchByKeyword_verifyResultsFound() throws Exception {
        KnowledgeEntryCreateDTO dto = knowledgeDTO(
                "EXPERIENCE", "经验总结", "独特搜索标题ABC", "独特搜索内容", "独特关键词XYZ");
        mockMvc.perform(post("/api/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/knowledge/search")
                        .param("q", "独特关键词XYZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].title").value("独特搜索标题ABC"));
    }

    @Test
    void searchWithNoResults_emptyList() throws Exception {
        mockMvc.perform(get("/api/knowledge/search")
                        .param("q", "完全不存在的随机字符串ZZZQQQ999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void markHelpful_helpfulCountIncremented() throws Exception {
        KnowledgeEntryCreateDTO dto = knowledgeDTO(
                "GENERAL", "FAQ", "有帮助测试", "有帮助内容", "帮助");
        MvcResult createResult = mockMvc.perform(post("/api/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        Long id = extractId(createResult);

        mockMvc.perform(post("/api/knowledge/{id}/helpful", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/knowledge/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.helpfulCount").value(1));
    }

    @Test
    void knowledgeStats_totalEntriesGreaterThanZero() throws Exception {
        KnowledgeEntryCreateDTO dto = knowledgeDTO(
                "GENERAL", "FAQ", "统计测试知识", "统计测试内容", "统计");
        mockMvc.perform(post("/api/knowledge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/knowledge/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEntries", greaterThan(0)))
                .andExpect(jsonPath("$.data.byCategory", not(anEmptyMap())));
    }

    @Test
    void importFromTicket_createsKnowledgeEntryFromResolution() throws Exception {
        Long projectId = createProject("KB-IMPORT-001");

        AfterSalesTicketCreateDTO ticketDto = new AfterSalesTicketCreateDTO();
        ticketDto.setProjectId(projectId);
        ticketDto.setTitle("打印异常问题");
        ticketDto.setDescription("打印功能故障无法使用");
        ticketDto.setChannel("WEB");
        ticketDto.setIntentType("FAULT");
        ticketDto.setReporterName("张三");
        ticketDto.setReporterContact("13800000000");

        MvcResult ticketResult = mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDto)))
                .andExpect(status().isOk())
                .andReturn();
        Long ticketId = extractId(ticketResult);

        mockMvc.perform(put("/api/after-sales-tickets/{id}/resolve", ticketId)
                        .param("resolution", "已修复打印配置问题")
                        .param("knowledgeCreated", "false"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/knowledge/import-from-ticket/{ticketId}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("EXPERIENCE"))
                .andExpect(jsonPath("$.data.layer").value("问题方案"))
                .andExpect(jsonPath("$.data.title").value("打印异常问题"))
                .andExpect(jsonPath("$.data.content", containsString("已修复打印配置问题")))
                .andExpect(jsonPath("$.data.source", containsString("售后工单")));
    }

    // ========================= Agent Decision Tests =========================

    @Test
    void logAgentDecision_verifySaved() throws Exception {
        Map<String, Object> body = Map.of(
                "agentCode", "DISPATCH_AGENT",
                "projectId", 1,
                "triggerType", "AUTO",
                "inputSummary", "测试输入摘要",
                "outputSummary", "测试输出摘要",
                "modelUsed", "gpt-4",
                "executionTimeMs", 1200
        );

        mockMvc.perform(post("/api/agent-decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentCode").value("DISPATCH_AGENT"))
                .andExpect(jsonPath("$.data.triggerType").value("AUTO"))
                .andExpect(jsonPath("$.data.inputSummary").value("测试输入摘要"))
                .andExpect(jsonPath("$.data.outputSummary").value("测试输出摘要"))
                .andExpect(jsonPath("$.data.modelUsed").value("gpt-4"))
                .andExpect(jsonPath("$.data.executionTimeMs").value(1200))
                .andExpect(jsonPath("$.data.humanReviewed").value(false));
    }

    @Test
    void reviewDecision_verifyReviewResultSaved() throws Exception {
        Map<String, Object> body = Map.of(
                "agentCode", "PLAN_AGENT",
                "triggerType", "MANUAL",
                "inputSummary", "审核测试输入",
                "outputSummary", "审核测试输出",
                "modelUsed", "gpt-3.5",
                "executionTimeMs", 800
        );

        MvcResult createResult = mockMvc.perform(post("/api/agent-decisions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andReturn();
        Long decisionId = extractId(createResult);

        mockMvc.perform(put("/api/agent-decisions/{id}/review", decisionId)
                        .param("reviewResult", "APPROVED")
                        .param("reviewComment", "审核通过备注"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.humanReviewed").value(true))
                .andExpect(jsonPath("$.data.reviewResult").value("APPROVED"))
                .andExpect(jsonPath("$.data.reviewComment").value("审核通过备注"));
    }

    @Test
    void agentPerformance_verifyTotalDecisionsCount() throws Exception {
        String agentCode = "PERF_TEST_AGENT";

        for (int i = 0; i < 3; i++) {
            Map<String, Object> body = Map.of(
                    "agentCode", agentCode,
                    "triggerType", "AUTO",
                    "inputSummary", "性能测试输入" + i,
                    "outputSummary", "性能测试输出" + i,
                    "modelUsed", "gpt-4",
                    "executionTimeMs", 500 + i * 100
            );
            mockMvc.perform(post("/api/agent-decisions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(body)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/agent-decisions/performance/{agentCode}", agentCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.agentCode").value(agentCode))
                .andExpect(jsonPath("$.data.totalDecisions").value(3))
                .andExpect(jsonPath("$.data.avgExecutionTimeMs", greaterThan(0.0)));
    }
}
