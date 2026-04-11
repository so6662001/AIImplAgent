package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProjectStatus;
import com.aimpl.domain.assist.dto.SupportTicketCreateDTO;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.dto.TraineeProfileCreateDTO;
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
class DeliveryReportAgentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectMapper projectMapper;

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
        dto.setRemark("交付报告测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createTrainee(Long projectId, String name, boolean kaUser) throws Exception {
        TraineeProfileCreateDTO dto = new TraineeProfileCreateDTO();
        dto.setProjectId(projectId);
        dto.setEmployeeName(name);
        dto.setRole("操作员");
        dto.setDepartment("IT部");
        dto.setKaUser(kaUser);
        MvcResult result = mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private void submitExam(Long projectId, Long traineeId, String module, int score) throws Exception {
        ExamRecordCreateDTO dto = new ExamRecordCreateDTO();
        dto.setProjectId(projectId);
        dto.setTraineeId(traineeId);
        dto.setModule(module);
        dto.setExamType("基础级");
        dto.setScore(score);
        dto.setRequiredCourse(true);
        mockMvc.perform(post("/api/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());
    }

    private Long createTicket(Long projectId, String title, String category) throws Exception {
        SupportTicketCreateDTO dto = new SupportTicketCreateDTO();
        dto.setProjectId(projectId);
        dto.setTitle(title);
        dto.setDescription("测试描述_" + title);
        dto.setCategory(category);
        MvcResult result = mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private MvcResult autoGenerate(Long projectId) throws Exception {
        return mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
    }

    // ========================= Test 1 =========================
    @Test
    void autoGenerate_emptyProject_still8Sections() throws Exception {
        Long projectId = createProject("DR-001");

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sections", hasSize(8)))
                .andExpect(jsonPath("$.data.sections[0].sectionNumber").value(1))
                .andExpect(jsonPath("$.data.sections[7].sectionNumber").value(8));
    }

    // ========================= Test 2 =========================
    @Test
    void autoGenerate_withTraineesAndExams_section4Populated() throws Exception {
        Long projectId = createProject("DR-002");
        Long trainee1 = createTrainee(projectId, "学员A", true);
        Long trainee2 = createTrainee(projectId, "学员B", false);
        submitExam(projectId, trainee1, "采购管理", 85);
        submitExam(projectId, trainee2, "采购管理", 55);

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[3].sectionTitle").value("培训完成情况"))
                .andExpect(jsonPath("$.data.sections[3].dataPoints.traineeCount").value(2))
                .andExpect(jsonPath("$.data.sections[3].dataPoints.kaCount").value(1))
                .andExpect(jsonPath("$.data.sections[3].dataPoints.totalExams").value(2))
                .andExpect(jsonPath("$.data.sections[3].dataPoints.passedExams").value(1))
                .andExpect(jsonPath("$.data.sections[3].content", containsString("培训学员总数：2人")));
    }

    // ========================= Test 3 =========================
    @Test
    void autoGenerate_reportPersisted_autoGeneratedTrue() throws Exception {
        Long projectId = createProject("DR-003");

        MvcResult result = autoGenerate(projectId);
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        Long reportId = data.path("reportId").asLong();

        mockMvc.perform(get("/api/delivery-reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.autoGenerated").value(true))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    // ========================= Test 4 =========================
    @Test
    void autoGenerate_withData_overallScorePositive() throws Exception {
        Long projectId = createProject("DR-004");
        Long trainee = createTrainee(projectId, "学员X", true);
        submitExam(projectId, trainee, "销售管理", 90);

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallScore", greaterThan(0.0)));
    }

    // ========================= Test 5 =========================
    @Test
    void autoGenerate_section7_listsOpenTickets() throws Exception {
        Long projectId = createProject("DR-005");
        createTicket(projectId, "操作问题A", "OPERATION");
        createTicket(projectId, "配置问题B", "CONFIG");

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[6].sectionTitle").value("遗留问题与建议"))
                .andExpect(jsonPath("$.data.sections[6].content", containsString("操作问题A")))
                .andExpect(jsonPath("$.data.sections[6].content", containsString("配置问题B")))
                .andExpect(jsonPath("$.data.sections[6].dataPoints.openTicketCount").value(2));
    }

    // ========================= Test 6 =========================
    @Test
    void autoGenerate_section8_has5MaintenanceItems() throws Exception {
        Long projectId = createProject("DR-006");

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[7].sectionTitle").value("后续维护计划"))
                .andExpect(jsonPath("$.data.sections[7].content", containsString("售后跟进期")))
                .andExpect(jsonPath("$.data.sections[7].content", containsString("定期回访")))
                .andExpect(jsonPath("$.data.sections[7].content", containsString("版本升级")))
                .andExpect(jsonPath("$.data.sections[7].content", containsString("知识库更新")))
                .andExpect(jsonPath("$.data.sections[7].content", containsString("培训补强")));
    }

    // ========================= Test 7 =========================
    @Test
    void autoGenerate_sectionTitlesMatchDesign() throws Exception {
        Long projectId = createProject("DR-007");

        mockMvc.perform(post("/api/delivery-reports/auto-generate/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[0].sectionTitle").value("项目概况"))
                .andExpect(jsonPath("$.data.sections[1].sectionTitle").value("交付成果总结"))
                .andExpect(jsonPath("$.data.sections[2].sectionTitle").value("目标达成分析"))
                .andExpect(jsonPath("$.data.sections[3].sectionTitle").value("培训完成情况"))
                .andExpect(jsonPath("$.data.sections[4].sectionTitle").value("工程师工作记录"))
                .andExpect(jsonPath("$.data.sections[5].sectionTitle").value("项目过程质量"))
                .andExpect(jsonPath("$.data.sections[6].sectionTitle").value("遗留问题与建议"))
                .andExpect(jsonPath("$.data.sections[7].sectionTitle").value("后续维护计划"));
    }

    // ========================= Test 8 =========================
    @Test
    void autoGenerate_twice_createsTwoReports() throws Exception {
        Long projectId = createProject("DR-008");

        MvcResult result1 = autoGenerate(projectId);
        MvcResult result2 = autoGenerate(projectId);

        JsonNode data1 = objectMapper.readTree(result1.getResponse().getContentAsString()).path("data");
        JsonNode data2 = objectMapper.readTree(result2.getResponse().getContentAsString()).path("data");

        long reportId1 = data1.path("reportId").asLong();
        long reportId2 = data2.path("reportId").asLong();
        assert reportId1 != reportId2 : "Two auto-generates should create two different reports";

        mockMvc.perform(get("/api/delivery-reports").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].autoGenerated", everyItem(is(true))));
    }
}
