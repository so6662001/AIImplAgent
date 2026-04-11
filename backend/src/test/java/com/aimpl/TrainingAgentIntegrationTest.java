package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.common.enums.ProjectStatus;
import com.aimpl.domain.project.dto.ProjectCreateDTO;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.dto.ExamGenerateRequestDTO;
import com.aimpl.domain.training.dto.ExamRecordCreateDTO;
import com.aimpl.domain.training.dto.RequiredCourseCreateDTO;
import com.aimpl.domain.training.dto.TraineeProfileCreateDTO;
import com.aimpl.domain.training.dto.TrainingDailyLogCreateDTO;
import com.aimpl.domain.workforce.dto.EngineerCreateDTO;
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
class TrainingAgentIntegrationTest {

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
        dto.setRemark("培训Agent测试");
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

    private Long createEngineer(String code, String name) throws Exception {
        EngineerCreateDTO dto = new EngineerCreateDTO();
        dto.setEngineerCode(code);
        dto.setName(name);
        dto.setLevel("SENIOR");
        dto.setSkills("Java,ERP");
        dto.setJoinDate(LocalDate.of(2023, 1, 1));
        dto.setPhone("13800138001");
        dto.setEmail(code.toLowerCase() + "@test.com");
        MvcResult result = mockMvc.perform(post("/api/engineers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private void createRequiredCourse(String module, int sortOrder) throws Exception {
        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModule(module);
        dto.setKaRequired(true);
        dto.setSortOrder(sortOrder);
        mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());
    }

    private void setProjectStatus(Long projectId, ProjectStatus status) {
        Project project = projectMapper.selectById(projectId);
        project.setStatus(status);
        projectMapper.updateById(project);
    }

    private void createTrainingLog(Long projectId, LocalDate date, String topic) throws Exception {
        TrainingDailyLogCreateDTO dto = new TrainingDailyLogCreateDTO();
        dto.setProjectId(projectId);
        dto.setLogDate(date);
        dto.setTopic(topic);
        dto.setTrainerName("张讲师");
        dto.setAttendeeCount(10);
        dto.setSignInCompleted(true);
        dto.setCoursewareUploaded(true);
        dto.setPlanUploaded(true);
        dto.setSummaryUploaded(false);
        dto.setExamConducted(true);
        dto.setDailyReportSubmitted(true);
        mockMvc.perform(post("/api/training-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk());
    }

    // ========================= Test 1: Generate exam for 采购管理+基础级 =========================

    @Test
    void generateExam_purchaseBasic_returns10Questions() throws Exception {
        ExamGenerateRequestDTO dto = new ExamGenerateRequestDTO();
        dto.setProjectId(1L);
        dto.setModule("采购管理");
        dto.setDifficulty("基础级");

        mockMvc.perform(post("/api/training/exam-generator/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.module").value("采购管理"))
                .andExpect(jsonPath("$.data.difficulty").value("基础级"))
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.totalScore").value(100))
                .andExpect(jsonPath("$.data.questions", hasSize(10)))
                .andExpect(jsonPath("$.data.questions[?(@.questionType=='CHOICE')]", hasSize(4)))
                .andExpect(jsonPath("$.data.questions[?(@.questionType=='TRUE_FALSE')]", hasSize(2)))
                .andExpect(jsonPath("$.data.questions[?(@.questionType=='OPERATION')]", hasSize(3)))
                .andExpect(jsonPath("$.data.questions[?(@.questionType=='SCENARIO')]", hasSize(1)));
    }

    // ========================= Test 2: Generate exam for unknown module =========================

    @Test
    void generateExam_unknownModule_returnsFallbackQuestions() throws Exception {
        ExamGenerateRequestDTO dto = new ExamGenerateRequestDTO();
        dto.setProjectId(1L);
        dto.setModule("未知模块XYZ");
        dto.setDifficulty("基础级");

        mockMvc.perform(post("/api/training/exam-generator/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalQuestions").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.questions", hasSize(greaterThan(0))));
    }

    // ========================= Test 3: Weak point analysis with failed exams =========================

    @Test
    void weakPointAnalysis_withFailedExams_identifiesWeakModules() throws Exception {
        Long projectId = createProject("WP-001");
        createRequiredCourse("采购管理", 1);
        createRequiredCourse("销售管理", 2);
        Long traineeId = createTrainee(projectId, "薄弱生", false);

        submitExam(projectId, traineeId, "采购管理", 85);
        submitExam(projectId, traineeId, "销售管理", 50);

        mockMvc.perform(get("/api/training/weak-points/{projectId}/{traineeId}", projectId, traineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.traineeId").value(traineeId.intValue()))
                .andExpect(jsonPath("$.data.totalModules").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.passedModules").value(1))
                .andExpect(jsonPath("$.data.weakModules", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.weakModules[?(@.module=='销售管理')]").isNotEmpty())
                .andExpect(jsonPath("$.data.remediationPlan", hasSize(greaterThan(0))));
    }

    // ========================= Test 4: Weak point analysis, all passed =========================

    @Test
    void weakPointAnalysis_allPassed_noWeakModules() throws Exception {
        Long projectId = createProject("WP-002");
        createRequiredCourse("采购管理", 1);
        createRequiredCourse("销售管理", 2);
        Long traineeId = createTrainee(projectId, "优秀生", false);

        submitExam(projectId, traineeId, "采购管理", 90);
        submitExam(projectId, traineeId, "销售管理", 85);

        mockMvc.perform(get("/api/training/weak-points/{projectId}/{traineeId}", projectId, traineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passedModules").value(2))
                .andExpect(jsonPath("$.data.weakModules", hasSize(0)));
    }

    // ========================= Test 5: Trainee progress recalculation =========================

    @Test
    void traineeProgress_afterExamSubmit_progressUpdated() throws Exception {
        Long projectId = createProject("TP-001");
        createRequiredCourse("采购管理", 1);
        createRequiredCourse("销售管理", 2);
        Long traineeId = createTrainee(projectId, "进步生", false);

        submitExam(projectId, traineeId, "采购管理", 80);

        mockMvc.perform(get("/api/trainees/{id}", traineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progressPercent").value(greaterThan(0)));
    }

    // ========================= Test 6: KA user low progress → HIGH risk =========================

    @Test
    void traineeProgress_kaLowProgress_riskHigh() throws Exception {
        Long projectId = createProject("TP-002");
        createRequiredCourse("采购管理", 1);
        createRequiredCourse("销售管理", 2);
        createRequiredCourse("库存管理", 3);
        Long kaTraineeId = createTrainee(projectId, "KA用户", true);

        submitExam(projectId, kaTraineeId, "采购管理", 50);

        mockMvc.perform(get("/api/trainees/{id}", kaTraineeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskLevel").value("HIGH"));
    }

    // ========================= Test 7: Daily tasks for TRAINING phase =========================

    @Test
    void dailyTasks_trainingPhase_includesPrepAndDocument() throws Exception {
        Long projectId = createProject("DT-001");
        setProjectStatus(projectId, ProjectStatus.TRAINING);
        Long engineerId = createEngineer("ENG-DT1", "任务工程师");

        mockMvc.perform(get("/api/workforce/daily-tasks")
                        .param("engineerId", engineerId.toString())
                        .param("projectId", projectId.toString())
                        .param("date", "2024-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phase").value("TRAINING"))
                .andExpect(jsonPath("$.data.tasks", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$.data.tasks[?(@.category=='PREP')]", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.tasks[?(@.category=='DOCUMENT')]", hasSize(greaterThan(0))));
    }

    // ========================= Test 8: Report draft generation =========================

    @Test
    void reportDraft_generatesNonEmptySections() throws Exception {
        Long projectId = createProject("RD-001");
        Long engineerId = createEngineer("ENG-RD1", "日报工程师");

        mockMvc.perform(get("/api/workforce/report-draft")
                        .param("engineerId", engineerId.toString())
                        .param("projectId", projectId.toString())
                        .param("date", "2024-07-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.engineerId").value(engineerId.intValue()))
                .andExpect(jsonPath("$.data.projectId").value(projectId.intValue()))
                .andExpect(jsonPath("$.data.summary").isNotEmpty())
                .andExpect(jsonPath("$.data.trainingSection").isNotEmpty())
                .andExpect(jsonPath("$.data.examSection").isNotEmpty())
                .andExpect(jsonPath("$.data.issuesSection").isNotEmpty())
                .andExpect(jsonPath("$.data.nextDayPlan").isNotEmpty());
    }

    // ========================= Test 9: Enhanced dashboard → traineeStatuses populated =========================

    @Test
    void enhancedDashboard_traineeStatusesPopulated() throws Exception {
        Long projectId = createProject("ED-001");
        createTrainee(projectId, "学员甲", false);
        createTrainee(projectId, "学员乙", true);

        mockMvc.perform(get("/api/training-dashboard/{projectId}/enhanced", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.traineeStatuses", hasSize(2)))
                .andExpect(jsonPath("$.data.traineeStatuses[0].name").isNotEmpty())
                .andExpect(jsonPath("$.data.traineeStatuses[1].name").isNotEmpty());
    }

    // ========================= Test 10: Enhanced dashboard → moduleStatuses have passRate =========================

    @Test
    void enhancedDashboard_moduleStatusesHavePassRate() throws Exception {
        Long projectId = createProject("ED-002");
        createRequiredCourse("采购管理", 1);
        Long traineeId = createTrainee(projectId, "考核生", false);
        submitExam(projectId, traineeId, "采购管理", 80);

        mockMvc.perform(get("/api/training-dashboard/{projectId}/enhanced", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.moduleStatuses", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.moduleStatuses[?(@.module=='采购管理')].passRate").isNotEmpty());
    }

    // ========================= Test 11: Enhanced dashboard → documentMatrix =========================

    @Test
    void enhancedDashboard_documentMatrix() throws Exception {
        Long projectId = createProject("ED-003");
        createTrainingLog(projectId, LocalDate.of(2024, 7, 1), "采购管理培训");
        createTrainingLog(projectId, LocalDate.of(2024, 7, 2), "销售管理培训");

        mockMvc.perform(get("/api/training-dashboard/{projectId}/enhanced", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.documentMatrix", hasSize(2)))
                .andExpect(jsonPath("$.data.documentMatrix[0].date").isNotEmpty())
                .andExpect(jsonPath("$.data.documentMatrix[0].planUploaded").value(true))
                .andExpect(jsonPath("$.data.documentMatrix[0].summaryUploaded").value(false));
    }

    // ========================= Test 12: Enhanced dashboard → riskWarnings for KA low progress =========================

    @Test
    void enhancedDashboard_riskWarnings_kaLowProgress() throws Exception {
        Long projectId = createProject("ED-004");
        createRequiredCourse("采购管理", 1);
        createRequiredCourse("销售管理", 2);
        createRequiredCourse("库存管理", 3);
        Long kaId = createTrainee(projectId, "风险KA", true);

        submitExam(projectId, kaId, "采购管理", 50);

        createTrainingLog(projectId, LocalDate.of(2024, 7, 1), "采购管理培训");

        mockMvc.perform(get("/api/training-dashboard/{projectId}/enhanced", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.riskWarnings", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.riskWarnings[0]", containsString("风险KA")));
    }
}
