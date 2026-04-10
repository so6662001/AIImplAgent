package com.aimpl.config;

import com.aimpl.domain.agent.entity.AgentConfig;
import com.aimpl.domain.agent.mapper.AgentConfigMapper;
import com.aimpl.domain.auth.entity.SysUser;
import com.aimpl.domain.auth.mapper.SysUserMapper;
import com.aimpl.domain.auth.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final AgentConfigMapper agentConfigMapper;

    @Override
    public void run(String... args) {
        initAdminUser();
        initAgentConfigs();
    }

    private void initAdminUser() {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (count > 0) {
            log.info("Admin user already exists, skipping initialization");
            return;
        }

        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(AuthService.encodePassword("admin123"));
        admin.setRealName("系统管理员");
        admin.setRole("ADMIN");
        admin.setEnabled(true);
        sysUserMapper.insert(admin);
        log.info("Default admin user created successfully");
    }

    private void initAgentConfigs() {
        Long count = agentConfigMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("Agent configs already exist, skipping initialization");
            return;
        }

        String[][] agents = {
                {"DISPATCH", "调度智能体", "负责项目调度与任务分配"},
                {"RESEARCH", "调研智能体", "负责客户调研与需求分析"},
                {"PLAN", "计划智能体", "负责项目计划制定与WBS分解"},
                {"TRAINING", "培训智能体", "负责培训管理与考核评估"},
                {"DATA_GOVERNANCE", "数据治理智能体", "负责基础数据梳理与导入"},
                {"GO_LIVE", "上线智能体", "负责上线检查与切换管理"},
                {"ASSIST", "辅助智能体", "负责日常问题解答与操作指导"},
                {"REPORT", "报告智能体", "负责项目报告生成与分析"},
                {"AFTER_SALES", "售后智能体", "负责售后服务与问题跟踪"},
                {"INPUT_ASSIST", "录入辅助智能体", "负责数据录入辅助与校验"},
                {"WORKFORCE", "人力智能体", "负责工程师管理与排班"},
                {"SIMULATION", "模拟演练智能体", "负责业务场景模拟与演练"}
        };

        for (String[] agent : agents) {
            AgentConfig config = new AgentConfig();
            config.setAgentCode(agent[0]);
            config.setAgentName(agent[1]);
            config.setDescription(agent[2]);
            config.setPromptTemplate("你是一个专业的钢铁行业ERP实施" + agent[1] + "，" + agent[2] + "。请根据用户的需求提供专业的建议和帮助。");
            config.setRagEnabled(false);
            config.setEnabled(true);
            agentConfigMapper.insert(config);
        }
        log.info("Default agent configs created: {} agents", agents.length);
    }
}
