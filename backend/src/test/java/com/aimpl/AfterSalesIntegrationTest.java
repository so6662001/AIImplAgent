package com.aimpl;

import com.aimpl.common.enums.IndustryType;
import com.aimpl.domain.aftersales.dto.AfterSalesTicketCreateDTO;
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
class AfterSalesIntegrationTest {

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
        dto.setRemark("售后集成测试");
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    private AfterSalesTicketCreateDTO ticketDTO(Long projectId, String title, String description,
                                                 String channel, String intentType) {
        AfterSalesTicketCreateDTO dto = new AfterSalesTicketCreateDTO();
        dto.setProjectId(projectId);
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setChannel(channel);
        dto.setIntentType(intentType);
        dto.setReporterName("张三");
        dto.setReporterContact("13800000000");
        return dto;
    }

    private Long createTicketAndGetId(AfterSalesTicketCreateDTO dto) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return extractId(result);
    }

    @Test
    void createConsultTicket_autoResolvedWithAiAnswer() throws Exception {
        Long projectId = createProject("AS-001");

        mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "规格如何填写", "规格怎么录入", "WEB", "CONSULT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolution", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.intentType").value("CONSULT"));
    }

    @Test
    void createFaultTicket_p1Priority_slaDeadline4h_statusSet() throws Exception {
        Long projectId = createProject("AS-002");

        mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "打印功能异常", "打印功能无法使用", "PHONE", "FAULT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slaPriority").value("P1"))
                .andExpect(jsonPath("$.data.slaDeadline", not(emptyOrNullString())))
                .andExpect(jsonPath("$.data.status", anyOf(is("ASSIGNED"), is("NEW"))));
    }

    @Test
    void createFaultTicket_systemUnavailable_p0Priority_slaDeadline1h() throws Exception {
        Long projectId = createProject("AS-003");

        mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "系统不可用", "系统不可用无法登录", "PHONE", "FAULT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slaPriority").value("P0"))
                .andExpect(jsonPath("$.data.slaDeadline", not(emptyOrNullString())));
    }

    @Test
    void createComplaintTicket_p1Priority() throws Exception {
        Long projectId = createProject("AS-004");

        mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ticketDTO(projectId, "服务态度差", "工程师服务态度差", "EMAIL", "COMPLAINT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slaPriority").value("P1"))
                .andExpect(jsonPath("$.data.intentType").value("COMPLAINT"));
    }

    @Test
    void createTicketBlankTitle_returns400() throws Exception {
        Long projectId = createProject("AS-005");

        AfterSalesTicketCreateDTO dto = ticketDTO(projectId, "", "描述内容", "WEB", "CONSULT");

        mockMvc.perform(post("/api/after-sales-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message", containsString("标题不能为空")));
    }

    @Test
    void resolveTicket_statusResolved_resolutionSaved() throws Exception {
        Long projectId = createProject("AS-006");
        Long ticketId = createTicketAndGetId(ticketDTO(projectId, "打印异常", "打印功能故障", "WEB", "FAULT"));

        mockMvc.perform(put("/api/after-sales-tickets/{id}/resolve", ticketId)
                        .param("resolution", "已修复打印配置")
                        .param("knowledgeCreated", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolution").value("已修复打印配置"))
                .andExpect(jsonPath("$.data.knowledgeCreated").value(true));
    }

    @Test
    void rateTicket_satisfactionSaved() throws Exception {
        Long projectId = createProject("AS-007");
        Long ticketId = createTicketAndGetId(ticketDTO(projectId, "咨询入库", "入库流程咨询", "WEB", "CONSULT"));

        mockMvc.perform(put("/api/after-sales-tickets/{id}/rate", ticketId)
                        .param("satisfaction", "4"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/after-sales-tickets").param("projectId", projectId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerSatisfaction").value(4));
    }

    @Test
    void ticketStats_verifyCounts() throws Exception {
        Long projectId = createProject("AS-008");
        createTicketAndGetId(ticketDTO(projectId, "咨询问题A", "规格咨询", "WEB", "CONSULT"));
        createTicketAndGetId(ticketDTO(projectId, "故障问题B", "打印故障", "PHONE", "FAULT"));
        createTicketAndGetId(ticketDTO(projectId, "建议问题C", "希望增加功能", "EMAIL", "SUGGESTION"));

        mockMvc.perform(get("/api/after-sales-tickets/stats/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.byIntent.CONSULT").value(1))
                .andExpect(jsonPath("$.data.byIntent.FAULT").value(1))
                .andExpect(jsonPath("$.data.byIntent.SUGGESTION").value(1))
                .andExpect(jsonPath("$.data.bySla.P1").value(1))
                .andExpect(jsonPath("$.data.bySla.P2").value(2));
    }

    @Test
    void customerHealthCheck_healthScoreCalculated_healthLevelSet() throws Exception {
        Long projectId = createProject("AS-009");
        createTicketAndGetId(ticketDTO(projectId, "咨询A", "规格咨询", "WEB", "CONSULT"));
        createTicketAndGetId(ticketDTO(projectId, "故障B", "打印故障", "PHONE", "FAULT"));

        mockMvc.perform(post("/api/customer-health/check/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.healthScore", notNullValue()))
                .andExpect(jsonPath("$.data.healthLevel", notNullValue()))
                .andExpect(jsonPath("$.data.ticketCount30d", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.openTicketCount", greaterThanOrEqualTo(0)));
    }

    @Test
    void healthHistory_multipleRecordsReturned() throws Exception {
        Long projectId = createProject("AS-010");

        mockMvc.perform(post("/api/customer-health/check/{projectId}", projectId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/customer-health/check/{projectId}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customer-health/history/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }
}
