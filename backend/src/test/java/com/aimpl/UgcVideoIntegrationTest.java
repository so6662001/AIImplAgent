package com.aimpl;

import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.mapper.ClientUserMapper;
import com.aimpl.domain.uservideo.entity.UserVideo;
import com.aimpl.domain.uservideo.mapper.UserVideoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UgcVideoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClientUserMapper clientUserMapper;
    @Autowired private UserVideoMapper userVideoMapper;

    private Long publisherId;
    private Long learnerId;

    @BeforeEach
    void setUp() {
        ClientUser publisher = new ClientUser();
        publisher.setProjectId(1L);
        publisher.setEmployeeName("发布者张三");
        publisher.setRole("TRAINEE");
        publisher.setDepartment("技术部");
        publisher.setPoints(0);
        publisher.setEnabled(true);
        publisher.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        clientUserMapper.insert(publisher);
        publisherId = publisher.getId();

        ClientUser learner = new ClientUser();
        learner.setProjectId(1L);
        learner.setEmployeeName("学习者李四");
        learner.setRole("TRAINEE");
        learner.setDepartment("业务部");
        learner.setPoints(100);
        learner.setEnabled(true);
        learner.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        clientUserMapper.insert(learner);
        learnerId = learner.getId();
    }

    private String json(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private JsonNode parseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private Map<String, Object> buildPublishPayload(String title, int pointsCost) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", "测试视频描述");
        map.put("categoryModule", "采购管理");
        map.put("videoUrl", "https://video.example.com/test.mp4");
        map.put("videoDuration", 600);
        map.put("pointsCost", pointsCost);
        return map;
    }

    private Long publishAndGetId(String title, int pointsCost) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/client/videos/publish")
                        .requestAttr("clientUserId", publisherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(buildPublishPayload(title, pointsCost))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        return parseData(result).path("id").asLong();
    }

    private void approveVideoAdmin(Long videoId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("approved", true);
        mockMvc.perform(put("/api/user-videos/{id}/approve", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // 1. Publish video → status=PENDING, publisher gets 10 bonus points
    @Test
    void publishVideo_statusPendingAndBonusPoints() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/client/videos/publish")
                        .requestAttr("clientUserId", publisherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(buildPublishPayload("我的教学视频", 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.title").value("我的教学视频"))
                .andReturn();

        int balance = objectMapper.readTree(
                mockMvc.perform(get("/api/client/videos/points/balance")
                                .requestAttr("clientUserId", publisherId))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
        ).path("data").asInt();
        assertEquals(10, balance);
    }

    // 2. Publish with blank title → 400
    @Test
    void publishBlankTitle_returns400() throws Exception {
        Map<String, Object> payload = buildPublishPayload("", 0);
        payload.put("title", "");

        mockMvc.perform(post("/api/client/videos/publish")
                        .requestAttr("clientUserId", publisherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest());
    }

    // 3. Approve video → status=APPROVED
    @Test
    void approveVideo_statusApproved() throws Exception {
        Long videoId = publishAndGetId("待审批视频", 0);

        Map<String, Object> body = new HashMap<>();
        body.put("approved", true);

        mockMvc.perform(put("/api/user-videos/{id}/approve", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedBy").isNotEmpty());
    }

    // 4. Reject video → status=REJECTED with reason
    @Test
    void rejectVideo_statusRejectedWithReason() throws Exception {
        Long videoId = publishAndGetId("低质量视频", 0);

        Map<String, Object> body = new HashMap<>();
        body.put("approved", false);
        body.put("rejectionReason", "内容不符合要求");

        mockMvc.perform(put("/api/user-videos/{id}/approve", videoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.approvalStatus").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("内容不符合要求"));
    }

    // 5. List approved videos → only APPROVED shown
    @Test
    void listApprovedVideos_onlyApproved() throws Exception {
        Long approvedId = publishAndGetId("通过的视频", 0);
        publishAndGetId("待审核的视频", 0);

        approveVideoAdmin(approvedId);

        mockMvc.perform(get("/api/client/videos/approved"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[?(@.title=='通过的视频')]").isNotEmpty())
                .andExpect(jsonPath("$.data[?(@.title=='待审核的视频')]").isEmpty());
    }

    // 6. Start learning free video → no points deducted
    @Test
    void startLearningFreeVideo_noPointsDeducted() throws Exception {
        Long videoId = publishAndGetId("免费视频", 0);
        approveVideoAdmin(videoId);

        int balanceBefore = clientUserMapper.selectById(learnerId).getPoints();

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.videoId").value(videoId.intValue()))
                .andExpect(jsonPath("$.data.pointsPaid").value(0));

        int balanceAfter = clientUserMapper.selectById(learnerId).getPoints();
        assertEquals(balanceBefore, balanceAfter);
    }

    // 7. Start learning paid video → learner points deducted, publisher points credited
    @Test
    void startLearningPaidVideo_pointsTransferred() throws Exception {
        Long videoId = publishAndGetId("付费视频", 20);
        approveVideoAdmin(videoId);

        int learnerBefore = clientUserMapper.selectById(learnerId).getPoints();
        int publisherBefore = clientUserMapper.selectById(publisherId).getPoints();

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pointsPaid").value(20));

        int learnerAfter = clientUserMapper.selectById(learnerId).getPoints();
        int publisherAfter = clientUserMapper.selectById(publisherId).getPoints();

        assertEquals(learnerBefore - 20, learnerAfter);
        assertEquals(publisherBefore + 20, publisherAfter);
    }

    // 8. Start learning insufficient points → 400 "积分不足"
    @Test
    void startLearningInsufficientPoints_returns400() throws Exception {
        Long videoId = publishAndGetId("昂贵视频", 50);
        approveVideoAdmin(videoId);

        ClientUser poorLearner = new ClientUser();
        poorLearner.setProjectId(1L);
        poorLearner.setEmployeeName("穷学生");
        poorLearner.setRole("TRAINEE");
        poorLearner.setDepartment("销售部");
        poorLearner.setPoints(10);
        poorLearner.setEnabled(true);
        poorLearner.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
        clientUserMapper.insert(poorLearner);

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", poorLearner.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("积分不足")));
    }

    // 9. Start learning same video twice → idempotent (no double charge)
    @Test
    void startLearningSameVideoTwice_idempotent() throws Exception {
        Long videoId = publishAndGetId("重复学习视频", 10);
        approveVideoAdmin(videoId);

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk());

        int balanceAfterFirst = clientUserMapper.selectById(learnerId).getPoints();

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk());

        int balanceAfterSecond = clientUserMapper.selectById(learnerId).getPoints();
        assertEquals(balanceAfterFirst, balanceAfterSecond);
    }

    // 10. Update learning progress → 90% auto-complete
    @Test
    void updateLearningProgress_autoCompleteAt90Percent() throws Exception {
        Long videoId = publishAndGetId("进度视频", 0);
        approveVideoAdmin(videoId);

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk());

        Map<String, Object> progressDto = new HashMap<>();
        progressDto.put("videoId", videoId);
        progressDto.put("watchedDuration", 540); // 540/600 = 90%

        mockMvc.perform(post("/api/client/videos/learning-progress")
                        .requestAttr("clientUserId", learnerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(progressDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.completed").value(true))
                .andExpect(jsonPath("$.data.completedAt").isNotEmpty());
    }

    // 11. Points balance → correct after transactions
    @Test
    void pointsBalance_correctAfterTransactions() throws Exception {
        Long videoId = publishAndGetId("积分余额视频", 15);
        approveVideoAdmin(videoId);

        mockMvc.perform(post("/api/client/videos/{videoId}/start-learning", videoId)
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk());

        MvcResult balanceResult = mockMvc.perform(get("/api/client/videos/points/balance")
                        .requestAttr("clientUserId", learnerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        int balance = objectMapper.readTree(
                balanceResult.getResponse().getContentAsString()
        ).path("data").asInt();
        assertEquals(100 - 15, balance);
    }

    // 12. Points history → transactions recorded
    @Test
    void pointsHistory_transactionsRecorded() throws Exception {
        publishAndGetId("历史记录视频", 0);

        MvcResult historyResult = mockMvc.perform(get("/api/client/videos/points/history")
                        .requestAttr("clientUserId", publisherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        JsonNode txList = objectMapper.readTree(
                historyResult.getResponse().getContentAsString()
        ).path("data");
        assertTrue(txList.size() >= 1);

        JsonNode firstTx = txList.get(0);
        assertEquals(publisherId.intValue(), firstTx.path("userId").asInt());
        assertEquals(10, firstTx.path("amount").asInt());
        assertNotNull(firstTx.path("description").asText());
    }
}
