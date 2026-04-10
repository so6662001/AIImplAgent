package com.aimpl.domain.qa.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.project.entity.Project;
import com.aimpl.domain.project.mapper.ProjectMapper;
import com.aimpl.domain.qa.dto.ClientLoginDTO;
import com.aimpl.domain.qa.dto.ClientLoginVO;
import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.mapper.ClientUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientAuthService extends ServiceImpl<ClientUserMapper, ClientUser> {

    private final ProjectMapper projectMapper;

    public ClientLoginVO clientLogin(ClientLoginDTO dto) {
        Project project = projectMapper.selectOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getProjectCode, dto.getProjectCode()));

        if (project == null) {
            throw new BizException("项目编号不存在: " + dto.getProjectCode());
        }

        ClientUser user = getOne(new LambdaQueryWrapper<ClientUser>()
                .eq(ClientUser::getProjectId, project.getId())
                .eq(ClientUser::getEmployeeName, dto.getEmployeeName()));

        if (user == null) {
            user = new ClientUser();
            user.setProjectId(project.getId());
            user.setEmployeeName(dto.getEmployeeName());
            user.setAccessToken(UUID.randomUUID().toString().replace("-", ""));
            user.setEnabled(true);
            save(user);
        } else if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BizException("该账户已被禁用");
        }

        user.setLastActiveTime(LocalDateTime.now());
        updateById(user);

        ClientLoginVO vo = new ClientLoginVO();
        vo.setAccessToken(user.getAccessToken());
        vo.setProjectId(project.getId());
        vo.setProjectName(project.getCustomerName());
        vo.setEmployeeName(user.getEmployeeName());
        vo.setRole(user.getRole());
        return vo;
    }

    public ClientUser validateToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<ClientUser>()
                .eq(ClientUser::getAccessToken, accessToken)
                .eq(ClientUser::getEnabled, true));
    }

    public void updateLastActiveTime(Long userId) {
        ClientUser user = new ClientUser();
        user.setId(userId);
        user.setLastActiveTime(LocalDateTime.now());
        updateById(user);
    }
}
