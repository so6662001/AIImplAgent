package com.aimpl.config;

import com.aimpl.domain.qa.entity.ClientUser;
import com.aimpl.domain.qa.service.ClientAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ClientAuthFilter extends OncePerRequestFilter {

    private final ClientAuthService clientAuthService;
    private final ObjectMapper objectMapper;
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = request.getRequestURI();

        if (!path.startsWith("/api/client/")) {
            return true;
        }

        if (path.startsWith("/api/client/auth/")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("X-Client-Token");

        if (token == null || token.isBlank()) {
            writeUnauthorized(response);
            return;
        }

        try {
            ClientUser user = clientAuthService.validateToken(token);
            if (user == null) {
                writeUnauthorized(response);
                return;
            }

            request.setAttribute("clientUserId", user.getId());
            request.setAttribute("projectId", user.getProjectId());
            request.setAttribute("employeeName", user.getEmployeeName());

            clientAuthService.updateLastActiveTime(user.getId());

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("Client token validation failed: {}", e.getMessage());
            writeUnauthorized(response);
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> body = Map.of(
                "code", 401,
                "message", "客户端令牌无效或已过期"
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
