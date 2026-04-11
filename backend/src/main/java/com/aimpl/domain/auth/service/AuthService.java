package com.aimpl.domain.auth.service;

import com.aimpl.common.exception.BizException;
import com.aimpl.domain.auth.dto.LoginDTO;
import com.aimpl.domain.auth.dto.LoginVO;
import com.aimpl.domain.auth.dto.RegisterDTO;
import com.aimpl.domain.auth.entity.SysUser;
import com.aimpl.domain.auth.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<SysUserMapper, SysUser> {

    private final JwtService jwtService;
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public LoginVO login(LoginDTO dto) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));

        if (user == null) {
            throw new BizException("用户名或密码错误");
        }

        if (!PASSWORD_ENCODER.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BizException("账户已被禁用");
        }

        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        String token = jwtService.generateToken(user);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setExpiresIn(jwtService.getExpiration() / 1000);
        return vo;
    }

    public SysUser register(RegisterDTO dto) {
        boolean exists = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())) > 0;
        if (exists) {
            throw new BizException("用户名已存在: " + dto.getUsername());
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole("VIEWER");
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setEnabled(true);
        try {
            save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("用户名已存在: " + dto.getUsername());
        }
        return user;
    }

    public SysUser getCurrentUser(String token) {
        String username = jwtService.getUsernameFromToken(token);
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    public static String encodePassword(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }
}
