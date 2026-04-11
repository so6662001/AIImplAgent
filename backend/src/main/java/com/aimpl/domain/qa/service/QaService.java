package com.aimpl.domain.qa.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.fieldhelp.entity.FieldHelpContent;
import com.aimpl.domain.fieldhelp.service.FieldHelpService;
import com.aimpl.domain.qa.dto.QaAskDTO;
import com.aimpl.domain.qa.dto.QaMessageVO;
import com.aimpl.domain.qa.dto.QaReplyDTO;
import com.aimpl.domain.qa.dto.QaSessionVO;
import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.entity.QaMessage;
import com.aimpl.domain.qa.entity.QaSession;
import com.aimpl.domain.qa.mapper.QaMessageMapper;
import com.aimpl.domain.qa.mapper.QaSessionMapper;
import com.aimpl.domain.knowledge.entity.KnowledgeEntry;
import com.aimpl.domain.knowledge.service.KnowledgeService;
import com.aimpl.domain.videolibrary.service.VideoLibraryService;
import com.aimpl.domain.videolibrary.vo.VideoClipMatchVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QaService {

    private final QaSessionMapper sessionMapper;
    private final QaMessageMapper messageMapper;
    private final ClientAuthService clientAuthService;
    private final FieldHelpService fieldHelpService;
    private final VideoLibraryService videoLibraryService;
    private final KnowledgeService knowledgeService;

    @Transactional
    public QaMessageVO ask(Long clientUserId, QaAskDTO dto) {
        ClientUser clientUser = clientAuthService.getById(clientUserId);
        if (clientUser == null) {
            throw new BizException("客户端用户不存在");
        }

        QaSession session;
        if (dto.getSessionId() != null) {
            session = sessionMapper.selectById(dto.getSessionId());
            if (session == null) {
                throw new BizException("会话不存在");
            }
            if (!session.getClientUserId().equals(clientUserId)) {
                throw new BizException("无权访问该会话");
            }
        } else {
            session = new QaSession();
            session.setProjectId(clientUser.getProjectId());
            session.setClientUserId(clientUserId);
            session.setStatus("ACTIVE");
            session.setMessageCount(0);
            String title = dto.getQuestion().length() > 60
                    ? dto.getQuestion().substring(0, 60)
                    : dto.getQuestion();
            session.setTitle(title);
            sessionMapper.insert(session);
        }

        QaMessage userMsg = new QaMessage();
        userMsg.setSessionId(session.getId());
        userMsg.setRole("USER");
        userMsg.setContent(dto.getQuestion());
        messageMapper.insert(userMsg);

        MockAnswer mockAnswer = generateContextAwareAnswer(
                dto.getQuestion(), dto.getCurrentPage(), dto.getCurrentField(),
                session.getProjectId());

        QaMessage aiMsg = new QaMessage();
        aiMsg.setSessionId(session.getId());
        aiMsg.setRole("ASSISTANT");
        aiMsg.setContent(mockAnswer.content);
        aiMsg.setRelatedModule(mockAnswer.relatedModule);
        aiMsg.setRelatedVideoUrl(mockAnswer.relatedVideoUrl);
        messageMapper.insert(aiMsg);

        session.setMessageCount(session.getMessageCount() + 2);
        sessionMapper.updateById(session);

        return toMessageVO(aiMsg);
    }

    public List<QaMessageVO> getSessionMessages(Long sessionId, Long clientUserId) {
        if (clientUserId != null) {
            QaSession session = sessionMapper.selectById(sessionId);
            if (session == null) {
                throw new BizException("会话不存在");
            }
            if (!session.getClientUserId().equals(clientUserId)) {
                throw new BizException("无权访问该会话");
            }
        }
        List<QaMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<QaMessage>()
                        .eq(QaMessage::getSessionId, sessionId)
                        .orderByAsc(QaMessage::getCreateTime));
        return messages.stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    public List<QaSessionVO> getUserSessions(Long clientUserId) {
        List<QaSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<QaSession>()
                        .eq(QaSession::getClientUserId, clientUserId)
                        .orderByDesc(QaSession::getUpdateTime));
        return sessions.stream().map(this::toSessionVO).collect(Collectors.toList());
    }

    public void rateMessage(Long messageId, Boolean helpful, Long clientUserId) {
        QaMessage msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BizException("消息不存在");
        }
        if (clientUserId != null) {
            QaSession session = sessionMapper.selectById(msg.getSessionId());
            if (session == null || !session.getClientUserId().equals(clientUserId)) {
                throw new BizException("无权操作该消息");
            }
        }
        msg.setHelpful(helpful);
        messageMapper.updateById(msg);
    }

    public List<QaSessionVO> getProjectSessions(Long projectId) {
        List<QaSession> sessions = sessionMapper.selectList(
                new LambdaQueryWrapper<QaSession>()
                        .eq(QaSession::getProjectId, projectId)
                        .orderByDesc(QaSession::getUpdateTime));
        return sessions.stream().map(this::toSessionVO).collect(Collectors.toList());
    }

    @Transactional
    public QaMessageVO adminReply(Long sessionId, QaReplyDTO dto) {
        QaSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException("会话不存在");
        }

        QaMessage msg = new QaMessage();
        msg.setSessionId(sessionId);
        msg.setRole("ASSISTANT");
        msg.setContent(dto.getContent());
        msg.setRelatedModule(dto.getRelatedModule());
        msg.setRelatedVideoUrl(dto.getRelatedVideoUrl());
        messageMapper.insert(msg);

        session.setMessageCount(session.getMessageCount() + 1);
        sessionMapper.updateById(session);

        return toMessageVO(msg);
    }

    private QaMessageVO toMessageVO(QaMessage msg) {
        QaMessageVO vo = new QaMessageVO();
        vo.setId(msg.getId());
        vo.setRole(msg.getRole());
        vo.setContent(msg.getContent());
        vo.setRelatedModule(msg.getRelatedModule());
        vo.setRelatedVideoUrl(msg.getRelatedVideoUrl());
        vo.setHelpful(msg.getHelpful());
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }

    private QaSessionVO toSessionVO(QaSession session) {
        QaSessionVO vo = new QaSessionVO();
        vo.setId(session.getId());
        vo.setProjectId(session.getProjectId());
        vo.setClientUserId(session.getClientUserId());
        vo.setTitle(session.getTitle());
        vo.setStatus(session.getStatus());
        vo.setMessageCount(session.getMessageCount());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }

    private static class MockAnswer {
        String content;
        String relatedModule;
        String relatedVideoUrl;

        MockAnswer(String content, String relatedModule) {
            this.content = content;
            this.relatedModule = relatedModule;
        }

        MockAnswer(String content, String relatedModule, String relatedVideoUrl) {
            this.content = content;
            this.relatedModule = relatedModule;
            this.relatedVideoUrl = relatedVideoUrl;
        }
    }

    private MockAnswer generateContextAwareAnswer(String question, String currentPage, String currentField,
                                                     Long projectId) {
        StringBuilder answerBuilder = new StringBuilder();
        String videoUrl = null;
        String knowledgeModule = null;

        if (currentPage != null && !currentPage.isBlank()
                && currentField != null && !currentField.isBlank()) {
            FieldHelpContent help = fieldHelpService.getHelp(currentPage, currentField);
            if (help != null) {
                answerBuilder.append("【字段帮助】").append(help.getHelpText());
                if (help.getFormatExample() != null && !help.getFormatExample().isBlank()) {
                    answerBuilder.append("  格式示例: ").append(help.getFormatExample());
                }
                if (help.getCommonErrors() != null && !help.getCommonErrors().isBlank()) {
                    answerBuilder.append("  常见错误: ").append(help.getCommonErrors());
                }
                answerBuilder.append("\n\n");
            }
        }

        List<VideoClipMatchVO> clips = videoLibraryService.matchClips(
                currentPage, currentField, question);
        if (!clips.isEmpty()) {
            VideoClipMatchVO top = clips.get(0);
            videoUrl = top.getVideoUrl() + "#t=" + top.getStartSecond();
            answerBuilder.append("【相关视频】").append(top.getVideoTitle())
                    .append(" - ").append(top.getClipTitle())
                    .append("（").append(formatTime(top.getStartSecond()))
                    .append("~").append(formatTime(top.getEndSecond())).append("）\n\n");
        }

        List<KnowledgeEntry> knowledgeResults = knowledgeService.search(question, null, projectId);
        if (!knowledgeResults.isEmpty()) {
            KnowledgeEntry topEntry = knowledgeResults.get(0);
            answerBuilder.append("【知识库】").append(topEntry.getTitle())
                    .append("：").append(topEntry.getContent()).append("\n\n");
            knowledgeModule = topEntry.getLayer() != null ? topEntry.getLayer() : topEntry.getCategory();
        }

        MockAnswer fallback = generateMockAnswer(question, currentPage, currentField);
        answerBuilder.append(fallback.content);

        String relatedModule = knowledgeModule != null ? knowledgeModule : fallback.relatedModule;
        return new MockAnswer(answerBuilder.toString(), relatedModule, videoUrl);
    }

    private String formatTime(Integer seconds) {
        if (seconds == null) return "0:00";
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private MockAnswer generateMockAnswer(String question, String currentPage, String currentField) {
        if (question.contains("规格")) {
            return new MockAnswer(
                    "规格的填写格式通常为：品名+材质+规格尺寸。例如：热轧卷板 Q235B 5.75×1500×C。请确保按照系统要求的格式录入，如有疑问可参考基础资料中的产品档案。",
                    "基础资料"
            );
        }
        if (question.contains("入库")) {
            return new MockAnswer(
                    "采购入库流程：1. 进入【采购管理】→【采购入库单】；2. 选择供应商和仓库；3. 录入商品明细（品名、规格、数量、重量、单价）；4. 确认无误后点击【保存】并【审核】。审核后库存将自动增加。",
                    "采购入库"
            );
        }
        if (question.contains("出库")) {
            return new MockAnswer(
                    "销售出库流程：1. 进入【销售管理】→【销售出库单】；2. 选择客户和出库仓库；3. 录入出库商品明细；4. 保存并审核后，库存将自动扣减，同时生成应收账款。",
                    "销售出库"
            );
        }
        if (question.contains("盘点")) {
            return new MockAnswer(
                    "库存盘点流程：1. 进入【库存管理】→【库存盘点】；2. 选择盘点仓库，系统会自动加载当前库存；3. 录入实盘数量和重量；4. 系统自动计算盘盈盘亏；5. 审核后自动调整库存账。",
                    "库存盘点"
            );
        }

        return new MockAnswer(
                "感谢您的提问，我已记录您的问题，实施工程师会尽快回复您。",
                null
        );
    }
}
