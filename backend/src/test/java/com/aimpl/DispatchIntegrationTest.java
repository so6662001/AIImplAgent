package com.aimpl;

import com.aimpl.common.enums.EngineerLevel;
import com.aimpl.common.enums.EngineerStatus;
import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.dispatch.dto.DispatchAssignDTO;
import com.aimpl.domain.dispatch.dto.DispatchRequestDTO;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.workforce.entity.Engineer;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DispatchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private EngineerMapper engineerMapper;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
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
        dto.setRemark("调度测试");

        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Engineer insertEngineer(String code, String name, EngineerLevel level,
                                     EngineerStatus status, String skills,
                                     BigDecimal compositeScore, Integer projectCount,
                                     BigDecimal idleRate) {
        Engineer eng = new Engineer();
        eng.setEngineerCode(code);
        eng.setName(name);
        eng.setLevel(level);
        eng.setCurrentStatus(status);
        eng.setSkills(skills);
        eng.setCompositeScore(compositeScore);
        eng.setMonthlyProjectCount(projectCount);
        eng.setMonthlyIdleRate(idleRate);
        eng.setJoinDate(LocalDate.of(2020, 1, 1));
        eng.setPhone("13900000001");
        eng.setEmail(code + "@test.com");
        engineerMapper.insert(eng);
        return eng;
    }

    // ========================= Test 1: Recommend with projectId =========================

    @Test
    void recommend_withProjectId_returnsSortedByMatchScore() throws Exception {
        Long projectId = createProject("DSP-001", "大型", "采购,销售,库存");

        insertEngineer("E-A", "张三", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购,销售,库存", new BigDecimal("90"), 0, new BigDecimal("0.5"));
        insertEngineer("E-B", "李四", EngineerLevel.SENIOR_PM, EngineerStatus.ON_PROJECT,
                "采购", new BigDecimal("70"), 1, new BigDecimal("0.1"));
        insertEngineer("E-C", "王五", EngineerLevel.EXPERT_PM, EngineerStatus.IDLE,
                "财务", new BigDecimal("60"), 0, new BigDecimal("0.3"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        MvcResult res = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recommendedPms", hasSize(3)))
                .andReturn();

        JsonNode pms = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("recommendedPms");
        int score0 = pms.get(0).path("matchScore").asInt();
        int score1 = pms.get(1).path("matchScore").asInt();
        int score2 = pms.get(2).path("matchScore").asInt();
        assert score0 >= score1 : "Results not sorted: " + score0 + " < " + score1;
        assert score1 >= score2 : "Results not sorted: " + score1 + " < " + score2;
    }

    // ========================= Test 2: Recommend with inline fields =========================

    @Test
    void recommend_withInlineFields_succeeds() throws Exception {
        insertEngineer("E-INL", "内联PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购,销售", new BigDecimal("80"), 0, new BigDecimal("0.4"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setCustomerName("测试客户");
        req.setIndustryType(IndustryType.STEEL_TRADER);
        req.setScale("中型");
        req.setModules(List.of("采购", "销售"));

        mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recommendedPms").isArray())
                .andExpect(jsonPath("$.data.scheduleSuggestion").exists())
                .andExpect(jsonPath("$.data.scheduleSuggestion.estimatedDurationDays").isNumber());
    }

    // ========================= Test 3: Skill matching =========================

    @Test
    void recommend_engineerWithMatchingSkills_scoresHigher() throws Exception {
        Long projectId = createProject("DSP-SKILL", "中型", "采购,销售,库存");

        Engineer matched = insertEngineer("E-MATCH", "匹配PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购,销售,库存", new BigDecimal("70"), 0, new BigDecimal("0.3"));
        Engineer unmatched = insertEngineer("E-NOMATCH", "不匹配PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "财务,质检", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        MvcResult res = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode pms = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("recommendedPms");

        int matchedScore = -1, unmatchedScore = -1;
        for (int i = 0; i < pms.size(); i++) {
            long engId = pms.get(i).path("engineerId").asLong();
            if (engId == matched.getId()) matchedScore = pms.get(i).path("matchScore").asInt();
            if (engId == unmatched.getId()) unmatchedScore = pms.get(i).path("matchScore").asInt();
        }
        assert matchedScore > unmatchedScore :
                "Matched engineer score (" + matchedScore + ") should be > unmatched (" + unmatchedScore + ")";
    }

    // ========================= Test 4: Availability scoring =========================

    @Test
    void recommend_idleEngineer_scoresHigherThanOnProject() throws Exception {
        Long projectId = createProject("DSP-AVAIL", "中型", "采购");

        Engineer idle = insertEngineer("E-IDLE", "空闲PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));
        Engineer busy = insertEngineer("E-BUSY", "忙碌PM", EngineerLevel.PM, EngineerStatus.ON_PROJECT,
                "采购", new BigDecimal("70"), 2, new BigDecimal("0.3"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        MvcResult res = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode pms = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("recommendedPms");
        int idleScore = -1, busyScore = -1;
        for (int i = 0; i < pms.size(); i++) {
            long engId = pms.get(i).path("engineerId").asLong();
            if (engId == idle.getId()) idleScore = pms.get(i).path("matchScore").asInt();
            if (engId == busy.getId()) busyScore = pms.get(i).path("matchScore").asInt();
        }
        assert idleScore > busyScore :
                "Idle engineer score (" + idleScore + ") should be > busy (" + busyScore + ")";
    }

    // ========================= Test 5: Performance scoring =========================

    @Test
    void recommend_higherCompositeScore_givesHigherMatchScore() throws Exception {
        Long projectId = createProject("DSP-PERF", "中型", "采购");

        Engineer highPerf = insertEngineer("E-HI", "高分PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("95"), 0, new BigDecimal("0.3"));
        Engineer lowPerf = insertEngineer("E-LO", "低分PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("40"), 0, new BigDecimal("0.3"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        MvcResult res = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode pms = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("recommendedPms");
        int hiScore = -1, loScore = -1;
        for (int i = 0; i < pms.size(); i++) {
            long engId = pms.get(i).path("engineerId").asLong();
            if (engId == highPerf.getId()) hiScore = pms.get(i).path("matchScore").asInt();
            if (engId == lowPerf.getId()) loScore = pms.get(i).path("matchScore").asInt();
        }
        assert hiScore > loScore :
                "High-perf score (" + hiScore + ") should be > low-perf (" + loScore + ")";
    }

    // ========================= Test 6: Schedule — large vs small =========================

    @Test
    void recommend_largeProject_gets90DaysBase() throws Exception {
        insertEngineer("E-SCH1", "调度PM1", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        Long largeId = createProject("DSP-LG", "大型", "采购,销售");
        Long smallId = createProject("DSP-SM", "小型", "采购,销售");

        DispatchRequestDTO reqLarge = new DispatchRequestDTO();
        reqLarge.setProjectId(largeId);

        DispatchRequestDTO reqSmall = new DispatchRequestDTO();
        reqSmall.setProjectId(smallId);

        MvcResult resLarge = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reqLarge)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult resSmall = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reqSmall)))
                .andExpect(status().isOk())
                .andReturn();

        int largeDuration = objectMapper.readTree(resLarge.getResponse().getContentAsString())
                .path("data").path("scheduleSuggestion").path("estimatedDurationDays").asInt();
        int smallDuration = objectMapper.readTree(resSmall.getResponse().getContentAsString())
                .path("data").path("scheduleSuggestion").path("estimatedDurationDays").asInt();

        assert largeDuration >= 90 : "Large project should have >= 90 days, got " + largeDuration;
        assert smallDuration >= 30 && smallDuration < 90 :
                "Small project should have ~30 days, got " + smallDuration;
        assert largeDuration > smallDuration :
                "Large (" + largeDuration + ") should be > small (" + smallDuration + ")";
    }

    // ========================= Test 7: MES adds extra days =========================

    @Test
    void recommend_mesModule_addsExtraDays() throws Exception {
        insertEngineer("E-MES", "MES-PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购,MES", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        Long withMes = createProject("DSP-MES", "中型", "采购,MES");
        Long withoutMes = createProject("DSP-NOMES", "中型", "采购,销售");

        DispatchRequestDTO reqMes = new DispatchRequestDTO();
        reqMes.setProjectId(withMes);

        DispatchRequestDTO reqNoMes = new DispatchRequestDTO();
        reqNoMes.setProjectId(withoutMes);

        MvcResult resMes = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reqMes)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult resNoMes = mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(reqNoMes)))
                .andExpect(status().isOk())
                .andReturn();

        int mesDuration = objectMapper.readTree(resMes.getResponse().getContentAsString())
                .path("data").path("scheduleSuggestion").path("estimatedDurationDays").asInt();
        int noMesDuration = objectMapper.readTree(resNoMes.getResponse().getContentAsString())
                .path("data").path("scheduleSuggestion").path("estimatedDurationDays").asInt();

        assert mesDuration > noMesDuration :
                "MES duration (" + mesDuration + ") should be > non-MES (" + noMesDuration + ")";
        assert mesDuration - noMesDuration >= 20 :
                "MES should add at least 20 days, diff=" + (mesDuration - noMesDuration);
    }

    // ========================= Test 8: 7 milestones generated =========================

    @Test
    void recommend_generates7Milestones() throws Exception {
        insertEngineer("E-MS", "里程碑PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        Long projectId = createProject("DSP-MILE", "中型", "采购,销售");

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleSuggestion.milestones", hasSize(7)))
                .andExpect(jsonPath("$.data.scheduleSuggestion.milestones[0].name").value("调研完成"))
                .andExpect(jsonPath("$.data.scheduleSuggestion.milestones[6].name").value("交付验收"));
    }

    // ========================= Test 9: Assign PM =========================

    @Test
    void assign_updatesProjectAndEngineer() throws Exception {
        Long projectId = createProject("DSP-ASN", "中型", "采购");

        Engineer eng = insertEngineer("E-ASN", "待分配PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        DispatchAssignDTO dto = new DispatchAssignDTO();
        dto.setProjectId(projectId);
        dto.setEngineerId(eng.getId());

        mockMvc.perform(post("/api/dispatch/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pmId").value(eng.getId().intValue()));

        Project updated = projectMapper.selectById(projectId);
        assert updated.getPmId().equals(eng.getId()) :
                "Project pmId should be " + eng.getId() + ", got " + updated.getPmId();

        Engineer updatedEng = engineerMapper.selectById(eng.getId());
        assert updatedEng.getCurrentStatus() == EngineerStatus.ON_PROJECT :
                "Engineer status should be ON_PROJECT, got " + updatedEng.getCurrentStatus();
        assert updatedEng.getCurrentProjectId().equals(projectId) :
                "Engineer currentProjectId should be " + projectId;
        assert updatedEng.getMonthlyProjectCount() == 1 :
                "Monthly project count should be 1, got " + updatedEng.getMonthlyProjectCount();
    }

    // ========================= Test 10: Assign invalid projectId =========================

    @Test
    void assign_invalidProjectId_returns400() throws Exception {
        Engineer eng = insertEngineer("E-INV-P", "测试PM", EngineerLevel.PM, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        DispatchAssignDTO dto = new DispatchAssignDTO();
        dto.setProjectId(999999L);
        dto.setEngineerId(eng.getId());

        mockMvc.perform(post("/api/dispatch/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("项目不存在")));
    }

    // ========================= Test 11: Assign invalid engineerId =========================

    @Test
    void assign_invalidEngineerId_returns400() throws Exception {
        Long projectId = createProject("DSP-INV-E", "中型", "采购");

        DispatchAssignDTO dto = new DispatchAssignDTO();
        dto.setProjectId(projectId);
        dto.setEngineerId(999999L);

        mockMvc.perform(post("/api/dispatch/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("工程师不存在")));
    }

    // ========================= Test 12: No PMs available =========================

    @Test
    void recommend_noPmEngineers_returnsEmptyList() throws Exception {
        Long projectId = createProject("DSP-NOPM", "中型", "采购");

        insertEngineer("E-JR", "初级工程师", EngineerLevel.JUNIOR, EngineerStatus.IDLE,
                "采购", new BigDecimal("70"), 0, new BigDecimal("0.3"));

        DispatchRequestDTO req = new DispatchRequestDTO();
        req.setProjectId(projectId);

        mockMvc.perform(post("/api/dispatch/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recommendedPms", hasSize(0)));
    }
}
