package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.mapper.ClientUserMapper;
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
class SmartInputIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientUserMapper clientUserMapper;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    private Long createVideoAndGetId() throws Exception {
        Map<String, Object> video = Map.of(
                "module", "采购管理",
                "functionName", "采购入库",
                "title", "采购入库操作视频",
                "videoUrl", "https://example.com/video1.mp4",
                "duration", 300
        );
        MvcResult result = mockMvc.perform(post("/api/video-library/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(video)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    // ========================= 1. Create video resource =========================

    @Test
    void createVideo_happyPath() throws Exception {
        Map<String, Object> video = Map.of(
                "module", "采购管理",
                "functionName", "采购入库",
                "title", "采购入库操作视频",
                "videoUrl", "https://example.com/video1.mp4",
                "duration", 300
        );

        mockMvc.perform(post("/api/video-library/videos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(video)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.module").value("采购管理"))
                .andExpect(jsonPath("$.data.functionName").value("采购入库"))
                .andExpect(jsonPath("$.data.title").value("采购入库操作视频"))
                .andExpect(jsonPath("$.data.videoUrl").value("https://example.com/video1.mp4"))
                .andExpect(jsonPath("$.data.duration").value(300))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    // ========================= 2. Create clip for video =========================

    @Test
    void createClip_happyPath() throws Exception {
        Long videoId = createVideoAndGetId();

        Map<String, Object> clip = new HashMap<>();
        clip.put("videoId", videoId);
        clip.put("clipTitle", "填写供应商字段");
        clip.put("startSecond", 30);
        clip.put("endSecond", 90);
        clip.put("relatedPage", "采购入库单");
        clip.put("relatedField", "供应商");
        clip.put("operationStep", "在供应商下拉框中选择对应供应商");

        mockMvc.perform(post("/api/video-library/videos/{videoId}/clips", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(clip)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.videoId").value(videoId.intValue()))
                .andExpect(jsonPath("$.data.clipTitle").value("填写供应商字段"))
                .andExpect(jsonPath("$.data.startSecond").value(30))
                .andExpect(jsonPath("$.data.endSecond").value(90));
    }

    // ========================= 3. Match clips by page+field (exact) =========================

    @Test
    void matchClips_byPageAndField_exactMatch() throws Exception {
        Long videoId = createVideoAndGetId();

        Map<String, Object> clip = new HashMap<>();
        clip.put("videoId", videoId);
        clip.put("clipTitle", "填写供应商");
        clip.put("startSecond", 30);
        clip.put("endSecond", 90);
        clip.put("relatedPage", "采购入库单");
        clip.put("relatedField", "供应商");
        mockMvc.perform(post("/api/video-library/videos/{videoId}/clips", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(clip)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/video-library/clips/match")
                        .param("page", "采购入库单")
                        .param("field", "供应商"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].matchType").value("EXACT"))
                .andExpect(jsonPath("$.data[0].relatedPage").value("采购入库单"))
                .andExpect(jsonPath("$.data[0].relatedField").value("供应商"));
    }

    // ========================= 4. Match clips by keyword =========================

    @Test
    void matchClips_byKeyword_keywordMatch() throws Exception {
        Long videoId = createVideoAndGetId();

        Map<String, Object> clip = new HashMap<>();
        clip.put("videoId", videoId);
        clip.put("clipTitle", "仓库选择操作");
        clip.put("startSecond", 100);
        clip.put("endSecond", 150);
        clip.put("relatedPage", "采购入库单");
        clip.put("relatedField", "仓库");
        clip.put("operationStep", "选择目标仓库并确认库位");
        mockMvc.perform(post("/api/video-library/videos/{videoId}/clips", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(clip)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/video-library/clips/match")
                        .param("question", "仓库选择"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].matchType").value("KEYWORD"));
    }

    // ========================= 5. Create field help =========================

    @Test
    void createFieldHelp_happyPath() throws Exception {
        Map<String, Object> help = Map.of(
                "page", "采购入库单",
                "fieldName", "供应商",
                "helpText", "请选择已在供应商档案中维护的供应商",
                "formatExample", "如：上海宝钢集团",
                "commonErrors", "未在档案中维护供应商就直接填写"
        );

        mockMvc.perform(post("/api/field-help")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(help)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.page").value("采购入库单"))
                .andExpect(jsonPath("$.data.fieldName").value("供应商"))
                .andExpect(jsonPath("$.data.helpText").value("请选择已在供应商档案中维护的供应商"))
                .andExpect(jsonPath("$.data.formatExample").value("如：上海宝钢集团"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    // ========================= 6. Lookup field help by page+field =========================

    @Test
    void lookupFieldHelp_found() throws Exception {
        Map<String, Object> help = Map.of(
                "page", "采购入库单",
                "fieldName", "数量",
                "helpText", "请填写实际入库数量，支持小数"
        );
        mockMvc.perform(post("/api/field-help")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(help)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/field-help/lookup")
                        .param("page", "采购入库单")
                        .param("field", "数量"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value("采购入库单"))
                .andExpect(jsonPath("$.data.fieldName").value("数量"))
                .andExpect(jsonPath("$.data.helpText").value("请填写实际入库数量，支持小数"));
    }

    // ========================= 7. Lookup non-existent → 200 with null data =========================

    @Test
    void lookupFieldHelp_notFound_returns200WithNullData() throws Exception {
        mockMvc.perform(get("/api/field-help/lookup")
                        .param("page", "不存在的页面")
                        .param("field", "不存在的字段"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ========================= 8. Log input assist =========================

    @Test
    void logInputAssist_happyPath() throws Exception {
        Map<String, Object> log = Map.of(
                "page", "采购入库单",
                "field", "供应商",
                "triggerType", "HELP_CLICK",
                "question", "供应商怎么填",
                "resolved", true
        );

        mockMvc.perform(post("/api/input-assist/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(log)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.page").value("采购入库单"))
                .andExpect(jsonPath("$.data.field").value("供应商"))
                .andExpect(jsonPath("$.data.triggerType").value("HELP_CLICK"))
                .andExpect(jsonPath("$.data.resolved").value(true));
    }

    // ========================= 9. Get assist stats =========================

    @Test
    void getAssistStats_verifyCounts() throws Exception {
        Map<String, Object> log1 = Map.of(
                "page", "销售出库单",
                "field", "客户",
                "triggerType", "HELP_CLICK",
                "resolved", true
        );
        Map<String, Object> log2 = Map.of(
                "page", "销售出库单",
                "field", "客户",
                "triggerType", "FIELD_DWELL",
                "resolved", false
        );
        Map<String, Object> log3 = Map.of(
                "page", "销售出库单",
                "field", "仓库",
                "triggerType", "HELP_CLICK",
                "resolved", true
        );

        mockMvc.perform(post("/api/input-assist/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(log1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/input-assist/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(log2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/input-assist/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(log3)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/input-assist/logs/stats")
                        .param("page", "销售出库单"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAssists").value(3))
                .andExpect(jsonPath("$.data.byTriggerType.HELP_CLICK").value(2))
                .andExpect(jsonPath("$.data.byTriggerType.FIELD_DWELL").value(1))
                .andExpect(jsonPath("$.data.topFields", hasSize(2)))
                .andExpect(jsonPath("$.data.topFields[0].field").value("客户"))
                .andExpect(jsonPath("$.data.topFields[0].count").value(2));
    }

    // ========================= 10. QA ask with currentPage+currentField =========================

    @Test
    void qaAskWithFieldContext_includesFieldHelpText() throws Exception {
        ProjectCreateDTO projectDTO = new ProjectCreateDTO();
        projectDTO.setProjectCode("SI-QA-PRJ");
        projectDTO.setCustomerName("智能录入测试客户");
        projectDTO.setIndustryType(IndustryType.STEEL_TRADER);
        projectDTO.setScale("中型");
        projectDTO.setPmId(1L);
        projectDTO.setModules(List.of("采购"));
        projectDTO.setRegion("华东");
        projectDTO.setStartDate(LocalDate.of(2024, 6, 1));
        MvcResult projectResult = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(projectDTO)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode projectNode = objectMapper.readTree(projectResult.getResponse().getContentAsString());
        Long projectId = projectNode.path("data").path("id").asLong();

        ClientUser clientUser = new ClientUser();
        clientUser.setProjectId(projectId);
        clientUser.setEmployeeName("测试员工");
        clientUser.setAccessToken("test-token-abc");
        clientUser.setEnabled(true);
        clientUserMapper.insert(clientUser);

        Map<String, Object> helpData = Map.of(
                "page", "采购入库单",
                "fieldName", "供应商",
                "helpText", "请从供应商档案中选择已审核的供应商",
                "formatExample", "上海宝钢"
        );
        mockMvc.perform(post("/api/field-help")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(helpData)))
                .andExpect(status().isOk());

        Map<String, Object> askBody = Map.of(
                "question", "供应商字段怎么填写",
                "currentPage", "采购入库单",
                "currentField", "供应商"
        );
        mockMvc.perform(post("/api/client/qa/ask")
                        .requestAttr("clientUserId", clientUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(askBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", containsString("请从供应商档案中选择已审核的供应商")));
    }
}
