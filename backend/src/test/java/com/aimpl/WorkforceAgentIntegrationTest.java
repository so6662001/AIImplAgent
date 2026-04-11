package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
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

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkforceAgentIntegrationTest {

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

    private Long createProject(String code, Long pmId) throws Exception {
        ProjectCreateDTO dto = new ProjectCreateDTO();
        dto.setProjectCode(code);
        dto.setCustomerName("客户_" + code);
        dto.setIndustryType(IndustryType.STEEL_TRADER);
        dto.setScale("大型");
        dto.setPmId(pmId);
        dto.setModules(List.of("采购", "销售"));
        dto.setRegion("华东");
        dto.setStartDate(LocalDate.of(2024, 6, 1));
        dto.setRemark("人力集成测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createEngineer(String code, String name, String level) throws Exception {
        EngineerCreateDTO dto = new EngineerCreateDTO();
        dto.setEngineerCode(code);
        dto.setName(name);
        dto.setLevel(level);
        dto.setSkills("Java,ERP");
        dto.setJoinDate(LocalDate.of(2023, 1, 1));
        MvcResult result = mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private void createProjectEvaluation(Long projectId, int schedule, int quality,
                                          int csat, int process, int cost) throws Exception {
        ProjectEvaluationCreateDTO dto = new ProjectEvaluationCreateDTO();
        dto.setProjectId(projectId);
        dto.setScheduleScore(schedule);
        dto.setQualityScore(quality);
        dto.setCsatScore(csat);
        dto.setProcessScore(process);
        dto.setCostScore(cost);
        mockMvc.perform(post("/api/project-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void dashboard_noEngineers_allCountsZero() throws Exception {
        mockMvc.perform(get("/api/workforce/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalEngineers").value(0))
                .andExpect(jsonPath("$.data.onProjectCount").value(0))
                .andExpect(jsonPath("$.data.idleCount").value(0))
                .andExpect(jsonPath("$.data.trainingCount").value(0))
                .andExpect(jsonPath("$.data.leaveCount").value(0));
    }

    @Test
    void dashboard_withEngineers_statusCountsAndUtilization() throws Exception {
        createEngineer("WF-E01", "张工", "SENIOR");
        createEngineer("WF-E02", "李工", "MID");
        createEngineer("WF-E03", "王工", "JUNIOR");

        mockMvc.perform(get("/api/workforce/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalEngineers").value(3))
                .andExpect(jsonPath("$.data.utilizationRate", notNullValue()))
                .andExpect(jsonPath("$.data.idleRate", notNullValue()))
                .andExpect(jsonPath("$.data.engineers", hasSize(3)));
    }

    @Test
    void compositeScore_sixDimensionsWithCorrectWeights() throws Exception {
        Long engineerId = createEngineer("WF-CS01", "综合评分工程师", "SENIOR");

        mockMvc.perform(get("/api/workforce/composite-score/{id}", engineerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.engineerId").value(engineerId.intValue()))
                .andExpect(jsonPath("$.data.dimensions", hasSize(6)))
                .andExpect(jsonPath("$.data.dimensions[0].weight").value(0.35))
                .andExpect(jsonPath("$.data.dimensions[1].weight").value(0.20))
                .andExpect(jsonPath("$.data.dimensions[2].weight").value(0.15))
                .andExpect(jsonPath("$.data.dimensions[3].weight").value(0.10))
                .andExpect(jsonPath("$.data.dimensions[4].weight").value(0.10))
                .andExpect(jsonPath("$.data.dimensions[5].weight").value(0.10));
    }

    @Test
    void compositeScore_scoreBetween0And100() throws Exception {
        Long engineerId = createEngineer("WF-CS02", "范围评分工程师", "MID");

        mockMvc.perform(get("/api/workforce/composite-score/{id}", engineerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.compositeScore",
                        allOf(greaterThanOrEqualTo(0.0), lessThanOrEqualTo(100.0))));
    }

    @Test
    void compositeScore_highPqiEvaluations_higherDim1() throws Exception {
        Long engineerId = createEngineer("WF-CS03", "高PQI工程师", "SENIOR");

        Long projectId = createProject("WF-PQI-HIGH", engineerId);
        createProjectEvaluation(projectId, 95, 95, 95, 95, 95);

        MvcResult highResult = mockMvc.perform(get("/api/workforce/composite-score/{id}", engineerId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode highData = objectMapper.readTree(highResult.getResponse().getContentAsString());
        double highDim1 = highData.path("data").path("dimensions").get(0).path("score").asDouble();

        Long baseEngineerId = createEngineer("WF-CS03B", "基准工程师", "SENIOR");
        MvcResult baseResult = mockMvc.perform(get("/api/workforce/composite-score/{id}", baseEngineerId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode baseData = objectMapper.readTree(baseResult.getResponse().getContentAsString());
        double baseDim1 = baseData.path("data").path("dimensions").get(0).path("score").asDouble();

        assert highDim1 > baseDim1 :
                "Engineer with high PQI evaluations should have higher dim1 (" + highDim1 + ") than base (" + baseDim1 + ")";
    }

    @Test
    void compositeScore_lowScore_growthSuggestionsNotEmpty() throws Exception {
        Long engineerId = createEngineer("WF-CS04", "低分工程师", "JUNIOR");

        Long projectId = createProject("WF-LOW-SCORE", engineerId);
        createProjectEvaluation(projectId, 20, 30, 25, 35, 15);

        mockMvc.perform(get("/api/workforce/composite-score/{id}", engineerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.growthSuggestions", not(empty())));
    }

    @Test
    void monthlyReport_rankingsSortedByCompositeScoreDesc() throws Exception {
        Long eng1 = createEngineer("WF-MR01", "排名工程师A", "SENIOR");
        Long eng2 = createEngineer("WF-MR02", "排名工程师B", "MID");

        mockMvc.perform(get("/api/workforce/composite-score/{id}", eng1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/workforce/composite-score/{id}", eng2))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/workforce/monthly-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rankings", not(empty())))
                .andReturn();

        JsonNode rankings = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("rankings");

        for (int i = 0; i < rankings.size() - 1; i++) {
            double current = rankings.get(i).path("compositeScore").asDouble();
            double next = rankings.get(i + 1).path("compositeScore").asDouble();
            assert current >= next :
                    "Rankings should be sorted by compositeScore desc, but index " + i +
                            " (" + current + ") < index " + (i + 1) + " (" + next + ")";
        }
    }

    @Test
    void monthlyReport_talentOverviewCategories() throws Exception {
        createEngineer("WF-TO01", "人才总览A", "SENIOR");
        createEngineer("WF-TO02", "人才总览B", "JUNIOR");

        mockMvc.perform(get("/api/workforce/monthly-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.talentOverview", notNullValue()))
                .andExpect(jsonPath("$.data.talentOverview.promotionCandidates", notNullValue()))
                .andExpect(jsonPath("$.data.talentOverview.keyTrainingTargets", notNullValue()))
                .andExpect(jsonPath("$.data.talentOverview.needAttention", notNullValue()))
                .andExpect(jsonPath("$.data.talentOverview.skillGaps", notNullValue()));
    }
}
