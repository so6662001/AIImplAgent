package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.assist.dto.SupportTicketCreateDTO;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OnSiteAssistIntegrationTest {

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
        dto.setRemark("集成测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private SupportTicketCreateDTO ticketDTO(Long projectId, String title, String description, String category) {
        SupportTicketCreateDTO dto = new SupportTicketCreateDTO();
        dto.setProjectId(projectId);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCategory(category);
        return dto;
    }

    private Long createTicketAndGetId(SupportTicketCreateDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    @Test
    void createOperationTicket_levelL1_assignedToAI() throws Exception {
        Long projectId = createProject("OSA-001");

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "如何录入规格", "规格怎么填写", "OPERATION"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.level").value("L1"))
                .andExpect(jsonPath("$.data.assignedTo").value("AI助手"))
                .andExpect(jsonPath("$.data.aiSuggestion", not(emptyOrNullString())));
    }

    @Test
    void createConfigTicket_levelL2() throws Exception {
        Long projectId = createProject("OSA-002");

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "打印模板问题", "打印模板无法设置", "CONFIG"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.level").value("L2"))
                .andExpect(jsonPath("$.data.category").value("CONFIG"));
    }

    @Test
    void createBugTicket_levelL4_assignedToDevTeam() throws Exception {
        Long projectId = createProject("OSA-003");

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "系统崩溃", "点击保存后系统崩溃", "BUG"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.level").value("L4"))
                .andExpect(jsonPath("$.data.assignedTo").value("研发团队"));
    }

    @Test
    void createTicketWithUrgentKeyword_priorityUrgent() throws Exception {
        Long projectId = createProject("OSA-004");

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "紧急问题", "系统紧急故障需要修复", "OPERATION"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.priority").value("URGENT"));
    }

    @Test
    void createTicketBlankTitle_returns400() throws Exception {
        Long projectId = createProject("OSA-005");

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "", "描述内容", "OPERATION"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("标题不能为空")));
    }

    @Test
    void resolveTicket_statusResolved_resolutionSaved() throws Exception {
        Long projectId = createProject("OSA-006");
        Long ticketId = createTicketAndGetId(ticketDTO(projectId, "操作问题", "操作不了", "OPERATION"));

        mockMvc.perform(put("/api/support-tickets/{id}/resolve", ticketId)
                        .param("resolution", "已指导用户完成操作"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolution").value("已指导用户完成操作"));
    }

    @Test
    void listTicketsByProjectId() throws Exception {
        Long projectId = createProject("OSA-007");
        createTicketAndGetId(ticketDTO(projectId, "问题1", "描述1", "OPERATION"));
        createTicketAndGetId(ticketDTO(projectId, "问题2", "描述2", "CONFIG"));

        mockMvc.perform(get("/api/support-tickets").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void ticketStats_verifyCounts() throws Exception {
        Long projectId = createProject("OSA-008");
        Long id1 = createTicketAndGetId(ticketDTO(projectId, "工单A", "描述A", "OPERATION"));
        createTicketAndGetId(ticketDTO(projectId, "工单B", "描述B", "CONFIG"));
        createTicketAndGetId(ticketDTO(projectId, "工单C", "描述C", "BUG"));

        mockMvc.perform(put("/api/support-tickets/{id}/resolve", id1)
                        .param("resolution", "已处理"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/support-tickets/stats/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.open").value(2))
                .andExpect(jsonPath("$.data.resolved").value(1))
                .andExpect(jsonPath("$.data.byLevel.L1").value(1))
                .andExpect(jsonPath("$.data.byLevel.L4").value(1));
    }

    @Test
    void healthCheckOnEmptyProject_returnsAlerts() throws Exception {
        Long projectId = createProject("OSA-009");

        mockMvc.perform(post("/api/alerts/health-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void acknowledgeAlert_markedAsAcknowledged() throws Exception {
        Long projectId = createProject("OSA-010");

        MvcResult healthResult = mockMvc.perform(post("/api/alerts/health-check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        JsonNode alerts = objectMapper.readTree(healthResult.getResponse().getContentAsString()).path("data");
        Long alertId = alerts.get(0).path("id").asLong();

        mockMvc.perform(put("/api/alerts/{id}/acknowledge", alertId)
                        .param("acknowledgedBy", "测试人员"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/alerts").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + alertId + ")]").doesNotExist());
    }
}
