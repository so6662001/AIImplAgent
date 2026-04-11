package com.aimpl.domain.workforce.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.entity.ProjectPlan;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.project.mapper.ProjectPlanMapper;
import com.aimpl.domain.training.entity.ExamRecord;
import com.aimpl.domain.training.entity.TraineeProfile;
import com.aimpl.domain.training.entity.TrainingDailyLog;
import com.aimpl.domain.training.mapper.ExamRecordMapper;
import com.aimpl.domain.training.mapper.TraineeProfileMapper;
import com.aimpl.domain.training.mapper.TrainingDailyLogMapper;
import com.aimpl.domain.workforce.entity.EngineerWorklog;
import com.aimpl.domain.workforce.mapper.EngineerMapper;
import com.aimpl.domain.workforce.mapper.EngineerWorklogMapper;
import com.aimpl.domain.workforce.vo.DailyReportDraftVO;
import com.aimpl.domain.workforce.vo.DailyTaskPlanVO;
import com.aimpl.domain.workforce.vo.TaskItemVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyTaskGeneratorService {

    private final ProjectMapper projectMapper;
    private final ProjectPlanMapper projectPlanMapper;
    private final EngineerMapper engineerMapper;
    private final EngineerWorklogMapper engineerWorklogMapper;
    private final TrainingDailyLogMapper trainingDailyLogMapper;
    private final ExamRecordMapper examRecordMapper;
    private final TraineeProfileMapper traineeProfileMapper;

    public DailyTaskPlanVO generateDailyTasks(Long engineerId, Long projectId, LocalDate date) {
        if (engineerMapper.selectById(engineerId) == null) {
            throw new BizException("工程师不存在: " + engineerId);
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        String phase = project.getStatus() != null ? project.getStatus().name() : "PENDING";

        ProjectPlan plan = projectPlanMapper.selectOne(
                new LambdaQueryWrapper<ProjectPlan>()
                        .eq(ProjectPlan::getProjectId, projectId)
                        .orderByDesc(ProjectPlan::getCreateTime)
                        .last("LIMIT 1"));

        List<TaskItemVO> tasks = new ArrayList<>();
        List<String> nextDayPrep = new ArrayList<>();

        switch (phase) {
            case "TRAINING" -> generateTrainingTasks(tasks, nextDayPrep, projectId, date);
            case "DATA_IMPORT" -> generateDataImportTasks(tasks, nextDayPrep);
            case "GO_LIVE" -> generateGoLiveTasks(tasks, nextDayPrep);
            default -> generateGenericTasks(tasks, nextDayPrep, phase);
        }

        DailyTaskPlanVO vo = new DailyTaskPlanVO();
        vo.setEngineerId(engineerId);
        vo.setProjectId(projectId);
        vo.setDate(date);
        vo.setPhase(phase);
        vo.setTasks(tasks);
        vo.setNextDayPrep(nextDayPrep);
        return vo;
    }

    public DailyReportDraftVO generateReportDraft(Long engineerId, Long projectId, LocalDate date) {
        if (engineerMapper.selectById(engineerId) == null) {
            throw new BizException("工程师不存在: " + engineerId);
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        TrainingDailyLog todayLog = trainingDailyLogMapper.selectOne(
                new LambdaQueryWrapper<TrainingDailyLog>()
                        .eq(TrainingDailyLog::getProjectId, projectId)
                        .eq(TrainingDailyLog::getLogDate, date)
                        .last("LIMIT 1"));

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<ExamRecord> todayExams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId)
                        .ge(ExamRecord::getExamTime, dayStart)
                        .lt(ExamRecord::getExamTime, dayEnd));

        EngineerWorklog worklog = engineerWorklogMapper.selectOne(
                new LambdaQueryWrapper<EngineerWorklog>()
                        .eq(EngineerWorklog::getEngineerId, engineerId)
                        .eq(EngineerWorklog::getWorkDate, date)
                        .last("LIMIT 1"));

        StringBuilder summary = new StringBuilder();
        StringBuilder trainingSection = new StringBuilder();
        StringBuilder examSection = new StringBuilder();
        StringBuilder issuesSection = new StringBuilder();
        StringBuilder nextDayPlan = new StringBuilder();

        if (todayLog != null) {
            trainingSection.append("培训主题: ").append(todayLog.getTopic()).append("\n");
            trainingSection.append("讲师: ").append(todayLog.getTrainerName()).append("\n");
            trainingSection.append("参训人数: ").append(todayLog.getAttendeeCount()).append("\n");
            trainingSection.append("签到完成: ").append(Boolean.TRUE.equals(todayLog.getSignInCompleted()) ? "是" : "否").append("\n");
            summary.append("今日完成").append(todayLog.getTopic()).append("培训");

            if (todayLog.getIssues() != null && !todayLog.getIssues().isBlank()) {
                issuesSection.append("培训问题: ").append(todayLog.getIssues()).append("\n");
            }
        } else {
            trainingSection.append("今日无培训安排\n");
            summary.append("今日无培训安排");
        }

        if (!todayExams.isEmpty()) {
            examSection.append("今日考核记录:\n");
            long passed = todayExams.stream().filter(ExamRecord::getPassed).count();
            examSection.append("  考核人次: ").append(todayExams.size()).append("\n");
            examSection.append("  通过人次: ").append(passed).append("\n");
            double avgScore = todayExams.stream().mapToInt(ExamRecord::getScore).average().orElse(0);
            examSection.append("  平均分: ").append(String.format("%.1f", avgScore)).append("\n");
            summary.append("，完成").append(todayExams.size()).append("人次考核");
        } else {
            examSection.append("今日无考核记录\n");
        }

        if (worklog != null) {
            if (worklog.getIssues() != null && !worklog.getIssues().isBlank()) {
                issuesSection.append("工作问题: ").append(worklog.getIssues()).append("\n");
            }
            if (worklog.getNextDayPlan() != null && !worklog.getNextDayPlan().isBlank()) {
                nextDayPlan.append(worklog.getNextDayPlan());
            }
        }

        if (issuesSection.length() == 0) {
            issuesSection.append("无问题\n");
        }
        if (nextDayPlan.length() == 0) {
            nextDayPlan.append("待安排");
        }

        DailyReportDraftVO vo = new DailyReportDraftVO();
        vo.setEngineerId(engineerId);
        vo.setProjectId(projectId);
        vo.setDate(date);
        vo.setSummary(summary.toString());
        vo.setTrainingSection(trainingSection.toString().trim());
        vo.setExamSection(examSection.toString().trim());
        vo.setIssuesSection(issuesSection.toString().trim());
        vo.setNextDayPlan(nextDayPlan.toString().trim());
        return vo;
    }

    private void generateTrainingTasks(List<TaskItemVO> tasks, List<String> nextDayPrep,
                                        Long projectId, LocalDate date) {
        tasks.add(buildTask("PREP", "准备培训课件", "根据培训计划准备当天课件和演示材料", "HIGH", "1小时"));
        tasks.add(buildTask("PREP", "配置演示环境", "检查并配置培训演示环境，确保系统可正常运行", "HIGH", "30分钟"));
        tasks.add(buildTask("PREP", "准备考核试卷", "根据当天培训内容准备考核试卷", "MEDIUM", "30分钟"));
        tasks.add(buildTask("EXECUTION", "上午培训", "按计划开展上午培训课程", "HIGH", "3小时"));
        tasks.add(buildTask("EXECUTION", "下午培训", "按计划开展下午培训课程", "HIGH", "3小时"));
        tasks.add(buildTask("DOCUMENT", "上传培训课件", "将当天培训课件上传至系统", "MEDIUM", "15分钟"));
        tasks.add(buildTask("DOCUMENT", "提交培训纪要", "编写并提交培训纪要，记录要点和问题", "MEDIUM", "30分钟"));
        tasks.add(buildTask("DOCUMENT", "提交工程师日报", "完成并提交当天工作日报", "MEDIUM", "20分钟"));

        List<ExamRecord> failedExams = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getProjectId, projectId)
                        .eq(ExamRecord::getPassed, false));

        if (!failedExams.isEmpty()) {
            List<Long> failedTraineeIds = failedExams.stream()
                    .map(ExamRecord::getTraineeId).distinct().collect(Collectors.toList());
            List<TraineeProfile> failedTrainees = traineeProfileMapper.selectList(
                    new LambdaQueryWrapper<TraineeProfile>()
                            .in(TraineeProfile::getId, failedTraineeIds));
            for (TraineeProfile t : failedTrainees) {
                tasks.add(buildTask("FOLLOWUP", "安排" + t.getEmployeeName() + "补学辅导",
                        "该学员考核未通过，需安排补学辅导", "HIGH", "1小时"));
            }
        }

        nextDayPrep.add("准备明天培训课件");
        nextDayPrep.add("检查学员出勤情况");
        nextDayPrep.add("整理今天培训反馈");
    }

    private void generateDataImportTasks(List<TaskItemVO> tasks, List<String> nextDayPrep) {
        tasks.add(buildTask("EXECUTION", "数据模板检查", "检查客户提供的数据模板是否符合要求", "HIGH", "1小时"));
        tasks.add(buildTask("EXECUTION", "数据清洗校验", "对导入数据进行清洗、去重和格式校验", "HIGH", "2小时"));
        tasks.add(buildTask("EXECUTION", "数据导入执行", "执行数据导入操作", "HIGH", "2小时"));
        tasks.add(buildTask("EXECUTION", "导入后验证", "验证导入数据的完整性和准确性", "HIGH", "1小时"));
        tasks.add(buildTask("DOCUMENT", "提交工程师日报", "完成并提交当天工作日报", "MEDIUM", "20分钟"));

        nextDayPrep.add("准备下一批数据导入模板");
        nextDayPrep.add("整理今天数据导入问题");
    }

    private void generateGoLiveTasks(List<TaskItemVO> tasks, List<String> nextDayPrep) {
        tasks.add(buildTask("EXECUTION", "上线检查清单执行", "逐项执行上线检查清单，确认各项准备就绪", "HIGH", "2小时"));
        tasks.add(buildTask("EXECUTION", "帐套启用确认", "确认帐套配置正确并执行启用操作", "HIGH", "1小时"));
        tasks.add(buildTask("EXECUTION", "现场跟进辅助", "在客户现场跟进首日运行情况并提供辅助", "HIGH", "4小时"));
        tasks.add(buildTask("DOCUMENT", "提交工程师日报", "完成并提交当天工作日报", "MEDIUM", "20分钟"));

        nextDayPrep.add("跟进上线后问题处理");
        nextDayPrep.add("准备用户操作答疑");
    }

    private void generateGenericTasks(List<TaskItemVO> tasks, List<String> nextDayPrep, String phase) {
        tasks.add(buildTask("EXECUTION", "项目跟进", "跟进项目当前阶段(" + phase + ")相关工作", "MEDIUM", "4小时"));
        tasks.add(buildTask("DOCUMENT", "提交工程师日报", "完成并提交当天工作日报", "MEDIUM", "20分钟"));

        nextDayPrep.add("制定明天工作计划");
    }

    private TaskItemVO buildTask(String category, String title, String description,
                                  String priority, String estimatedTime) {
        TaskItemVO task = new TaskItemVO();
        task.setCategory(category);
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setEstimatedTime(estimatedTime);
        return task;
    }
}
