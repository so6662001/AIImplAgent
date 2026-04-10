package com.aimpl.domain.training.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.RequiredCourse;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.RequiredCourseMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.vo.RemediationItemVO;
import com.aimpl.domain.training.vo.WeakModuleVO;
import com.aimpl.domain.training.vo.WeakPointAnalysisVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamWeakPointAnalysisService {

    private static final int PASS_SCORE = 70;

    private final TraineeProfileMapper traineeProfileMapper;
    private final ExamRecordMapper examRecordMapper;
    private final RequiredCourseMapper requiredCourseMapper;
    private final ProjectMapper projectMapper;

    public WeakPointAnalysisVO analyzeWeakPoints(Long projectId, Long traineeId) {
        TraineeProfile trainee = traineeProfileMapper.selectById(traineeId);
        if (trainee == null) {
            throw new BizException("学员不存在: " + traineeId);
        }
        if (!trainee.getProjectId().equals(projectId)) {
            throw new BizException("学员不属于该项目");
        }

        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        String industryType = project.getIndustryType() != null
                ? project.getIndustryType().name() : null;
        List<String> allModules = industryType != null
                ? requiredCourseMapper.selectList(
                        new LambdaQueryWrapper<RequiredCourse>()
                                .eq(RequiredCourse::getIndustryType, industryType))
                        .stream().map(RequiredCourse::getCourseModule)
                        .distinct().collect(Collectors.toList())
                : List.of();

        List<ExamRecord> exams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId)
                        .eq(ExamRecord::getTraineeId, traineeId));

        Map<String, List<ExamRecord>> byModule = exams.stream()
                .collect(Collectors.groupingBy(ExamRecord::getModule));

        List<WeakModuleVO> weakModules = new ArrayList<>();
        List<RemediationItemVO> remediationPlan = new ArrayList<>();
        int passedCount = 0;

        Set<String> examedModules = byModule.keySet();
        Set<String> allModuleSet = new LinkedHashSet<>(allModules);
        allModuleSet.addAll(examedModules);

        for (String module : allModuleSet) {
            List<ExamRecord> moduleExams = byModule.get(module);
            if (moduleExams == null || moduleExams.isEmpty()) {
                WeakModuleVO wm = new WeakModuleVO();
                wm.setModule(module);
                wm.setBestScore(0);
                wm.setAttempts(0);
                wm.setStatus("未考核");
                weakModules.add(wm);
                addRemediation(remediationPlan, module, "HIGH");
            } else {
                int bestScore = moduleExams.stream()
                        .mapToInt(ExamRecord::getScore).max().orElse(0);
                int attempts = moduleExams.size();
                if (bestScore >= PASS_SCORE) {
                    passedCount++;
                } else {
                    WeakModuleVO wm = new WeakModuleVO();
                    wm.setModule(module);
                    wm.setBestScore(bestScore);
                    wm.setAttempts(attempts);
                    wm.setStatus("未通过");
                    weakModules.add(wm);
                    addRemediation(remediationPlan, module, "MEDIUM");
                }
            }
        }

        WeakPointAnalysisVO vo = new WeakPointAnalysisVO();
        vo.setTraineeId(traineeId);
        vo.setTraineeName(trainee.getEmployeeName());
        vo.setTotalModules(allModuleSet.size());
        vo.setPassedModules(passedCount);
        vo.setWeakModules(weakModules);
        vo.setRemediationPlan(remediationPlan);
        return vo;
    }

    private void addRemediation(List<RemediationItemVO> plan, String module, String priority) {
        RemediationItemVO video = new RemediationItemVO();
        video.setType("VIDEO");
        video.setTitle(module + "操作全流程");
        video.setDescription("观看" + module + "模块的操作演示视频，掌握完整业务流程");
        video.setPriority(priority);
        plan.add(video);

        RemediationItemVO doc = new RemediationItemVO();
        doc.setType("DOCUMENT");
        doc.setTitle("《" + module + "操作手册》");
        doc.setDescription("阅读" + module + "操作手册相关章节，理解业务规则和操作要点");
        doc.setPriority(priority);
        plan.add(doc);

        RemediationItemVO practice = new RemediationItemVO();
        practice.setType("PRACTICE");
        practice.setTitle(module + "场景练习题");
        practice.setDescription("完成" + module + "模块的场景练习题，巩固操作技能");
        practice.setPriority("HIGH".equals(priority) ? "HIGH" : "MEDIUM");
        plan.add(practice);
    }
}
