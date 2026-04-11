package com.aimpl;

import com.aimpl.domain.course.dto.ChapterCreateDTO;
import com.aimpl.domain.course.dto.CourseCreateDTO;
import com.aimpl.domain.course.dto.LearningProgressUpdateDTO;
import com.aimpl.domain.onlineexam.dto.ExamAnswerSubmitDTO;
import com.aimpl.domain.onlineexam.dto.ExamPaperCreateDTO;
import com.aimpl.domain.onlineexam.entity.ExamPaper;
import com.aimpl.domain.onlineexam.service.OnlineExamService;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.vo.ExamQuestionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseAndExamIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private OnlineExamService onlineExamService;

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private JsonNode parseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private Long extractId(MvcResult result) throws Exception {
        return parseData(result).path("id").asLong();
    }

    private Long createCourse(String code, String name, int totalChapters) throws Exception {
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setCourseCode(code);
        dto.setCourseName(name);
        dto.setDescription("测试课程描述");
        dto.setIndustryType("STEEL_TRADER");
        dto.setModule("采购管理");
        dto.setTotalChapters(totalChapters);
        dto.setTotalDuration(3600);
        MvcResult result = mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long addChapter(Long courseId, int chapterNumber, String videoUrl, int videoDuration) throws Exception {
        ChapterCreateDTO dto = new ChapterCreateDTO();
        dto.setCourseId(courseId);
        dto.setChapterNumber(chapterNumber);
        dto.setChapterName("第" + chapterNumber + "章");
        dto.setVideoUrl(videoUrl);
        dto.setVideoDuration(videoDuration);
        MvcResult result = mockMvc.perform(post("/api/courses/{courseId}/chapters", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Long createExamPaper(Long projectId, String module, String difficulty) throws Exception {
        ExamPaperCreateDTO dto = new ExamPaperCreateDTO();
        dto.setProjectId(projectId);
        dto.setModule(module);
        dto.setDifficulty(difficulty);
        MvcResult result = mockMvc.perform(post("/api/online-exams/papers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private Map<Integer, String> extractCorrectAnswers(Long paperId) throws Exception {
        ExamPaper paper = onlineExamService.getById(paperId);
        List<ExamQuestionVO> questions = objectMapper.readValue(
                paper.getQuestions(), new TypeReference<>() {});
        Map<Integer, String> answers = new HashMap<>();
        for (ExamQuestionVO q : questions) {
            answers.put(q.getQuestionId(), q.getCorrectAnswer());
        }
        return answers;
    }

    // ========================= Test 1: Create course → verify fields =========================

    @Test
    void createCourse_verifyCoreFields() throws Exception {
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setCourseCode("CRS-001");
        dto.setCourseName("采购基础课程");
        dto.setDescription("采购基础");
        dto.setIndustryType("STEEL_TRADER");
        dto.setModule("采购管理");
        dto.setTotalChapters(5);
        dto.setTotalDuration(1800);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.courseCode").value("CRS-001"))
                .andExpect(jsonPath("$.data.courseName").value("采购基础课程"))
                .andExpect(jsonPath("$.data.totalChapters").value(5))
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    // ========================= Test 2: Create course duplicate code → 400 =========================

    @Test
    void createCourseDuplicateCode_returns400() throws Exception {
        createCourse("DUP-001", "课程A", 3);

        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setCourseCode("DUP-001");
        dto.setCourseName("课程B不同名称");
        dto.setTotalChapters(2);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ========================= Test 3: Add chapter → verify fields =========================

    @Test
    void addChapter_verifyFields() throws Exception {
        Long courseId = createCourse("CH-001", "章节课程", 3);

        mockMvc.perform(post("/api/courses/{courseId}/chapters", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new HashMap<>() {{
                            put("courseId", courseId);
                            put("chapterNumber", 1);
                            put("chapterName", "第一章-采购流程");
                            put("videoUrl", "https://video.example.com/ch1.mp4");
                            put("videoDuration", 600);
                        }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.chapterNumber").value(1))
                .andExpect(jsonPath("$.data.videoUrl").value("https://video.example.com/ch1.mp4"))
                .andExpect(jsonPath("$.data.videoDuration").value(600))
                .andExpect(jsonPath("$.data.courseId").value(courseId.intValue()));
    }

    // ========================= Test 4: List courses → returns created courses =========================

    @Test
    void listCourses_returnsCreatedCourses() throws Exception {
        createCourse("LIST-001", "课程甲", 2);
        createCourse("LIST-002", "课程乙", 3);

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[?(@.courseCode=='LIST-001')]").isNotEmpty())
                .andExpect(jsonPath("$.data[?(@.courseCode=='LIST-002')]").isNotEmpty());
    }

    // ========================= Test 5: Get course detail → returns course with chapters =========================

    @Test
    void getCourseDetail_returnsWithChapters() throws Exception {
        Long courseId = createCourse("DET-001", "详情课程", 3);
        addChapter(courseId, 1, "https://video.example.com/d1.mp4", 600);
        addChapter(courseId, 2, "https://video.example.com/d2.mp4", 900);

        mockMvc.perform(get("/api/courses/{id}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.courseCode").value("DET-001"))
                .andExpect(jsonPath("$.data.courseName").value("详情课程"))
                .andExpect(jsonPath("$.data.totalChapters").value(3))
                .andExpect(jsonPath("$.data.chapters", hasSize(2)))
                .andExpect(jsonPath("$.data.chapters[0].chapterNumber").value(1))
                .andExpect(jsonPath("$.data.chapters[1].chapterNumber").value(2));
    }

    // ========================= Test 6: Learning progress → auto-complete at 90% =========================

    @Test
    void learningProgress_autoCompletesAt90Percent() throws Exception {
        Long courseId = createCourse("LP-001", "学习课程", 1);
        Long chapterId = addChapter(courseId, 1, "https://video.example.com/lp.mp4", 1000);

        LearningProgressUpdateDTO dto = new LearningProgressUpdateDTO();
        dto.setClientUserId(999L);
        dto.setChapterId(chapterId);
        dto.setWatchedDuration(900);
        dto.setStatus("IN_PROGRESS");

        mockMvc.perform(post("/api/client/learning/progress")
                        .requestAttr("clientUserId", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.watchedDuration").value(900))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());
    }

    // ========================= Test 7: Create exam paper → verify fields =========================

    @Test
    void createExamPaper_verifyQuestionsAndStatus() throws Exception {
        ExamPaperCreateDTO dto = new ExamPaperCreateDTO();
        dto.setProjectId(1L);
        dto.setModule("采购管理");
        dto.setDifficulty("基础级");

        mockMvc.perform(post("/api/online-exams/papers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                .andExpect(jsonPath("$.data.questions").isNotEmpty())
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.totalScore").value(100))
                .andExpect(jsonPath("$.data.projectId").value(1));
    }

    // ========================= Test 8: Submit correct answers → high score, passed =========================

    @Test
    void submitExam_allCorrect_passedTrue() throws Exception {
        Long paperId = createExamPaper(1L, "采购管理", "基础级");
        Map<Integer, String> correctAnswers = extractCorrectAnswers(paperId);

        ExamAnswerSubmitDTO dto = new ExamAnswerSubmitDTO();
        dto.setPaperId(paperId);
        dto.setClientUserId(100L);
        dto.setAnswers(correctAnswers);
        dto.setDuration(1200);

        mockMvc.perform(post("/api/client/exams/submit")
                        .requestAttr("clientUserId", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(100))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.correctCount").value(10))
                .andExpect(jsonPath("$.data.wrongCount").value(0));
    }

    // ========================= Test 9: Submit wrong answers → low score, passed=false =========================

    @Test
    void submitExam_allWrong_passedFalseWithWrongQuestions() throws Exception {
        Long paperId = createExamPaper(1L, "销售管理", "基础级");

        Map<Integer, String> wrongAnswers = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            wrongAnswers.put(i, "WRONG_ANSWER_XYZ");
        }

        ExamAnswerSubmitDTO dto = new ExamAnswerSubmitDTO();
        dto.setPaperId(paperId);
        dto.setClientUserId(101L);
        dto.setAnswers(wrongAnswers);
        dto.setDuration(600);

        MvcResult result = mockMvc.perform(post("/api/client/exams/submit")
                        .requestAttr("clientUserId", 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(0))
                .andExpect(jsonPath("$.data.passed").value(false))
                .andExpect(jsonPath("$.data.wrongCount").value(10))
                .andReturn();

        JsonNode data = parseData(result);
        String wrongQuestionsStr = data.path("wrongQuestions").asText();
        assertFalse(wrongQuestionsStr.isEmpty(), "wrongQuestions should not be empty");
        List<JsonNode> wrongList = objectMapper.readValue(wrongQuestionsStr, new TypeReference<>() {});
        assertEquals(10, wrongList.size());
    }

    // ========================= Test 10: List papers by projectId =========================

    @Test
    void listPapersByProjectId_returnsCreatedPapers() throws Exception {
        Long projectId = 77L;
        createExamPaper(projectId, "采购管理", "基础级");
        createExamPaper(projectId, "销售管理", "基础级");

        mockMvc.perform(get("/api/online-exams/papers")
                        .param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ========================= Test 11: Get submission → verify score and wrong questions =========================

    @Test
    void getSubmission_verifyScoreAndWrongQuestions() throws Exception {
        Long paperId = createExamPaper(1L, "库存管理", "基础级");
        Map<Integer, String> correctAnswers = extractCorrectAnswers(paperId);
        correctAnswers.put(1, "WRONG");
        correctAnswers.put(2, "WRONG");

        ExamAnswerSubmitDTO dto = new ExamAnswerSubmitDTO();
        dto.setPaperId(paperId);
        dto.setClientUserId(200L);
        dto.setAnswers(correctAnswers);
        dto.setDuration(800);

        MvcResult submitResult = mockMvc.perform(post("/api/client/exams/submit")
                        .requestAttr("clientUserId", 200L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode submitData = parseData(submitResult);
        int score = submitData.path("score").asInt();
        assertTrue(score > 0, "Score should be > 0 since some answers are correct");
        assertTrue(score < 100, "Score should be < 100 since some answers are wrong");
        assertEquals(2, submitData.path("wrongCount").asInt());
        assertEquals(8, submitData.path("correctCount").asInt());

        Long clientUserId = 200L;
        mockMvc.perform(get("/api/client/exams/my-results")
                        .requestAttr("clientUserId", clientUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ========================= Test 12: Exam submission auto-creates ExamRecord =========================

    @Test
    void examSubmission_autoCreatesExamRecord() throws Exception {
        Long projectId = 88L;
        Long clientUserId = 300L;

        Long paperId = createExamPaper(projectId, "财务应收", "基础级");
        Map<Integer, String> correctAnswers = extractCorrectAnswers(paperId);

        ExamAnswerSubmitDTO dto = new ExamAnswerSubmitDTO();
        dto.setPaperId(paperId);
        dto.setClientUserId(clientUserId);
        dto.setAnswers(correctAnswers);
        dto.setDuration(900);

        mockMvc.perform(post("/api/client/exams/submit")
                        .requestAttr("clientUserId", clientUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passed").value(true));

        var records = examRecordMapper.selectList(
                new LambdaQueryWrapper<com.aimpl.domain.training.entity.ExamRecord>()
                        .eq(com.aimpl.domain.training.entity.ExamRecord::getProjectId, projectId)
                        .eq(com.aimpl.domain.training.entity.ExamRecord::getTraineeId, clientUserId));

        assertFalse(records.isEmpty(), "ExamRecord should be auto-created after exam submission");
        var record = records.get(0);
        assertEquals("ONLINE_EXAM", record.getExamType());
        assertEquals("财务应收", record.getModule());
        assertEquals(100, record.getScore());
        assertTrue(record.getPassed());
    }
}
