package com.aimpl.domain.qa.controller;

import com.aimpl.common.result.R;
import com.aimpl.domain.qa.dto.ClientLoginDTO;
import com.aimpl.domain.qa.dto.ClientLoginVO;
import com.aimpl.domain.qa.service.ClientAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class ClientAuthController {

    private final ClientAuthService clientAuthService;

    @PostMapping("/login")
    public R<ClientLoginVO> login(@Valid @RequestBody ClientLoginDTO dto) {
        return R.ok(clientAuthService.clientLogin(dto));
    }
}
