package com.aimpl.domain.onlineexam.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.onlineexam.dto.ExamAnswerSubmitDTO;
import com.aimpl.domain.onlineexam.dto.ExamPaperCreateDTO;
import com.aimpl.domain.onlineexam.entity.ExamPaper;
import com.aimpl.domain.onlineexam.entity.ExamSubmission;
import com.aimpl.domain.onlineexam.mapper.ExamPaperMapper;
import com.aimpl.domain.onlineexam.mapper.ExamSubmissionMapper;
import com.aimpl.domain.onlineexam.vo.WrongQuestionVO;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.service.ExamQuestionGeneratorService;
import com.aimpl.domain.training.vo.ExamQuestionVO;
import com.aimpl.domain.training.vo.GeneratedExamVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineExamService extends ServiceImpl<ExamPaperMapper, ExamPaper> {

    private static final int PASS_SCORE = 70;

    private final ExamSubmissionMapper submissionMapper;
    private final ExamQuestionGeneratorService questionGeneratorService;
    private final ExamRecordMapper examRecordMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ExamPaper createPaper(ExamPaperCreateDTO dto) {
        GeneratedExamVO exam = questionGeneratorService.generateExam(
                dto.getProjectId(), dto.getModule(), dto.getDifficulty());

        String questionsJson;
        try {
            questionsJson = objectMapper.writeValueAsString(exam.getQuestions());
        } catch (JsonProcessingException e) {
            throw new BizException("序列化试题失败");
        }

        ExamPaper paper = new ExamPaper();
        paper.setProjectId(dto.getProjectId());
        paper.setModule(dto.getModule());
        paper.setDifficulty(dto.getDifficulty());
        paper.setTitle(dto.getModule() + " - " + dto.getDifficulty() + " 在线考试");
        paper.setTotalQuestions(exam.getTotalQuestions());
        paper.setTotalScore(exam.getTotalScore());
        paper.setQuestions(questionsJson);
        paper.setCreatedFor(dto.getCreatedFor());
        paper.setStatus("CREATED");
        save(paper);
        return paper;
    }

    @Transactional
    public ExamSubmission submitAnswers(ExamAnswerSubmitDTO dto) {
        ExamPaper paper = getById(dto.getPaperId());
        if (paper == null) {
            throw new BizException("试卷不存在: " + dto.getPaperId());
        }
        if (!"CREATED".equals(paper.getStatus()) && !"IN_PROGRESS".equals(paper.getStatus())) {
            throw new BizException("试卷状态不允许提交: " + paper.getStatus());
        }

        List<ExamQuestionVO> questions;
        try {
            questions = objectMapper.readValue(paper.getQuestions(),
                    new TypeReference<List<ExamQuestionVO>>() {});
        } catch (JsonProcessingException e) {
            throw new BizException("解析试题失败");
        }

        int correctCount = 0;
        int wrongCount = 0;
        int score = 0;
        List<WrongQuestionVO> wrongQuestionList = new ArrayList<>();

        Map<Integer, String> userAnswers = dto.getAnswers();

        for (ExamQuestionVO q : questions) {
            String userAnswer = userAnswers.get(q.getQuestionId());
            if (userAnswer != null && userAnswer.equals(q.getCorrectAnswer())) {
                correctCount++;
                score += q.getScore();
            } else {
                wrongCount++;
                WrongQuestionVO wq = new WrongQuestionVO();
                wq.setQuestionId(q.getQuestionId());
                wq.setCorrectAnswer(q.getCorrectAnswer());
                wq.setUserAnswer(userAnswer != null ? userAnswer : "");
                wq.setQuestionText(q.getQuestionText());
                wrongQuestionList.add(wq);
            }
        }

        boolean passed = score >= PASS_SCORE;

        String answersJson;
        String wrongQuestionsJson;
        try {
            answersJson = objectMapper.writeValueAsString(userAnswers);
            wrongQuestionsJson = objectMapper.writeValueAsString(wrongQuestionList);
        } catch (JsonProcessingException e) {
            throw new BizException("序列化答案失败");
        }

        ExamSubmission submission = new ExamSubmission();
        submission.setPaperId(dto.getPaperId());
        submission.setClientUserId(dto.getClientUserId());
        submission.setAnswers(answersJson);
        submission.setScore(score);
        submission.setPassed(passed);
        submission.setCorrectCount(correctCount);
        submission.setWrongCount(wrongCount);
        submission.setWrongQuestions(wrongQuestionsJson);
        submission.setStartedAt(paper.getCreateTime());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
        submissionMapper.insert(submission);

        paper.setStatus("GRADED");
        updateById(paper);

        try {
            List<String> weakPoints = wrongQuestionList.stream()
                    .map(WrongQuestionVO::getQuestionText)
                    .limit(3)
                    .toList();

            ExamRecord record = new ExamRecord();
            record.setProjectId(paper.getProjectId());
            record.setTraineeId(dto.getClientUserId());
            record.setModule(paper.getModule());
            record.setExamType("ONLINE_EXAM");
            record.setScore(score);
            record.setPassed(passed);
            record.setRequiredCourse(false);
            record.setWeakPoints(String.join("; ", weakPoints));
            record.setExamTime(LocalDateTime.now());
            examRecordMapper.insert(record);
        } catch (Exception e) {
            log.warn("创建ExamRecord失败，不影响主流程: {}", e.getMessage());
        }

        return submission;
    }

    public ExamSubmission getSubmission(Long submissionId) {
        ExamSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException("提交记录不存在: " + submissionId);
        }
        return submission;
    }

    public List<ExamPaper> listPapers(Long projectId, Long clientUserId) {
        LambdaQueryWrapper<ExamPaper> qw = new LambdaQueryWrapper<>();
        if (projectId != null) {
            qw.eq(ExamPaper::getProjectId, projectId);
        }
        if (clientUserId != null) {
            qw.and(w -> w
                    .eq(ExamPaper::getCreatedFor, clientUserId)
                    .or().isNull(ExamPaper::getCreatedFor));
        }
        qw.orderByDesc(ExamPaper::getCreateTime);
        return list(qw);
    }

    public List<ExamSubmission> listSubmissions(Long projectId) {
        if (projectId == null) {
            return submissionMapper.selectList(new LambdaQueryWrapper<ExamSubmission>()
                    .orderByDesc(ExamSubmission::getCreateTime));
        }
        List<Long> paperIds = list(new LambdaQueryWrapper<ExamPaper>()
                .eq(ExamPaper::getProjectId, projectId)
                .select(ExamPaper::getId))
                .stream()
                .map(ExamPaper::getId)
                .toList();
        if (paperIds.isEmpty()) {
            return List.of();
        }
        return submissionMapper.selectList(new LambdaQueryWrapper<ExamSubmission>()
                .in(ExamSubmission::getPaperId, paperIds)
                .orderByDesc(ExamSubmission::getCreateTime));
    }

    public List<ExamSubmission> listMySubmissions(Long clientUserId) {
        return submissionMapper.selectList(new LambdaQueryWrapper<ExamSubmission>()
                .eq(ExamSubmission::getClientUserId, clientUserId)
                .orderByDesc(ExamSubmission::getCreateTime));
    }
}
