package com.aimpl.domain.auth.controller;

import com.aimpl.common.exception.BizException;
import com.aimpl.common.result.R;
import com.aimpl.domain.auth.dto.LoginDTO;
import com.aimpl.domain.auth.dto.LoginVO;
import com.aimpl.domain.auth.dto.RegisterDTO;
import com.aimpl.domain.auth.entity.SysUser;
import com.aimpl.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @PostMapping("/register")
    public R<SysUser> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok(authService.register(dto));
    }

    @GetMapping("/me")
    public R<SysUser> me(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new BizException(401, "未登录");
        }
        SysUser user = authService.getCurrentUser(token);
        if (user == null) {
            throw new BizException(401, "用户不存在");
        }
        return R.ok(user);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
