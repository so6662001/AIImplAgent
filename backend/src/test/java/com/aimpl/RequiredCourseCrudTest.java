package com.aimpl;

import com.aimpl.domain.training.dto.BatchToggleDTO;
import com.aimpl.domain.training.dto.RequiredCourseCreateDTO;
import com.aimpl.domain.training.dto.RequiredCourseUpdateDTO;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RequiredCourseCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequiredCourseMapper requiredCourseMapper;

    private RequiredCourse insertCourse(String industryType, String module, boolean kaRequired, int sortOrder) {
        RequiredCourse c = new RequiredCourse();
        c.setIndustryType(industryType);
        c.setCourseModule(module);
        c.setKaRequired(kaRequired);
        c.setSortOrder(sortOrder);
        requiredCourseMapper.insert(c);
        return c;
    }

    private RequiredCourseCreateDTO validCreateDTO(String module) {
        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModule(module);
        dto.setKaRequired(true);
        dto.setSortOrder(99);
        return dto;
    }

    private Long createCourseAndGetId(RequiredCourseCreateDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }

    // ==================== CREATE ====================

    @Test
    void create_happyPath_returnsFields() throws Exception {
        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModule("全新测试模块");
        dto.setKaRequired(true);
        dto.setSortOrder(50);

        mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.industryType").value("STEEL_TRADER"))
                .andExpect(jsonPath("$.data.courseModule").value("全新测试模块"))
                .andExpect(jsonPath("$.data.kaRequired").value(true))
                .andExpect(jsonPath("$.data.sortOrder").value(50));
    }

    @Test
    void create_invalidIndustryType_returns400() throws Exception {
        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("INVALID_TYPE");
        dto.setCourseModule("测试模块");
        dto.setKaRequired(true);
        dto.setSortOrder(1);

        mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_blankCourseModule_returns400() throws Exception {
        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModule("");
        dto.setKaRequired(true);

        mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void create_duplicate_returns400() throws Exception {
        insertCourse("STEEL_TRADER", "重复课程", true, 1);

        RequiredCourseCreateDTO dto = new RequiredCourseCreateDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModule("重复课程");
        dto.setKaRequired(true);
        dto.setSortOrder(1);

        mockMvc.perform(post("/api/required-courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== UPDATE ====================

    @Test
    void update_toggleKaRequired() throws Exception {
        RequiredCourseCreateDTO createDTO = validCreateDTO("切换必学标记");
        Long id = createCourseAndGetId(createDTO);

        RequiredCourseUpdateDTO updateDTO = new RequiredCourseUpdateDTO();
        updateDTO.setKaRequired(false);

        mockMvc.perform(put("/api/required-courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kaRequired").value(false));

        updateDTO.setKaRequired(true);
        mockMvc.perform(put("/api/required-courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kaRequired").value(true));
    }

    @Test
    void update_sortOrder() throws Exception {
        RequiredCourseCreateDTO createDTO = validCreateDTO("更新排序");
        Long id = createCourseAndGetId(createDTO);

        RequiredCourseUpdateDTO updateDTO = new RequiredCourseUpdateDTO();
        updateDTO.setKaRequired(true);
        updateDTO.setSortOrder(42);

        mockMvc.perform(put("/api/required-courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sortOrder").value(42));
    }

    @Test
    void update_nonExistentId_returns400() throws Exception {
        RequiredCourseUpdateDTO updateDTO = new RequiredCourseUpdateDTO();
        updateDTO.setKaRequired(false);

        mockMvc.perform(put("/api/required-courses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== DELETE ====================

    @Test
    void delete_happyPath() throws Exception {
        RequiredCourseCreateDTO createDTO = validCreateDTO("删除测试");
        Long id = createCourseAndGetId(createDTO);

        mockMvc.perform(delete("/api/required-courses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        RequiredCourseUpdateDTO updateDTO = new RequiredCourseUpdateDTO();
        updateDTO.setKaRequired(true);
        mockMvc.perform(put("/api/required-courses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_nonExistent_returns400() throws Exception {
        mockMvc.perform(delete("/api/required-courses/{id}", 999999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== LIST ====================

    @Test
    void list_byIndustryTypeFilter() throws Exception {
        insertCourse("STEEL_MILL", "列表筛选A", true, 1);
        insertCourse("STEEL_MILL", "列表筛选B", false, 2);
        insertCourse("PROCESSING_CENTER", "不应出现", true, 1);

        mockMvc.perform(get("/api/required-courses")
                        .param("industryType", "STEEL_MILL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data[0].industryType").value("STEEL_MILL"));
    }

    @Test
    void list_allWithNoFilter() throws Exception {
        insertCourse("STEEL_TRADER", "全量查A", true, 1);
        insertCourse("STEEL_MILL", "全量查B", true, 1);
        insertCourse("PROCESSING_CENTER", "全量查C", true, 1);
        insertCourse("INTEGRATED_SERVICE", "全量查D", true, 1);

        mockMvc.perform(get("/api/required-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(4)));
    }

    // ==================== BATCH TOGGLE ====================

    @Test
    void batchToggle_happyPath() throws Exception {
        insertCourse("INTEGRATED_SERVICE", "批量切换A", true, 1);
        insertCourse("INTEGRATED_SERVICE", "批量切换B", true, 2);

        BatchToggleDTO dto = new BatchToggleDTO();
        dto.setIndustryType("INTEGRATED_SERVICE");
        dto.setCourseModules(List.of("批量切换A", "批量切换B"));
        dto.setKaRequired(false);

        mockMvc.perform(post("/api/required-courses/batch-toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/required-courses")
                        .param("industryType", "INTEGRATED_SERVICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.courseModule == '批量切换A')].kaRequired",
                        hasItem(false)))
                .andExpect(jsonPath("$.data[?(@.courseModule == '批量切换B')].kaRequired",
                        hasItem(false)));
    }

    @Test
    void batchToggle_invalidIndustryType_returns400() throws Exception {
        BatchToggleDTO dto = new BatchToggleDTO();
        dto.setIndustryType("BAD_TYPE");
        dto.setCourseModules(List.of("基础资料"));
        dto.setKaRequired(false);

        mockMvc.perform(post("/api/required-courses/batch-toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void batchToggle_emptyCourseModules_returns400() throws Exception {
        BatchToggleDTO dto = new BatchToggleDTO();
        dto.setIndustryType("STEEL_TRADER");
        dto.setCourseModules(List.of());
        dto.setKaRequired(false);

        mockMvc.perform(post("/api/required-courses/batch-toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
