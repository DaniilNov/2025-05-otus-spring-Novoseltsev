package com.example.task.security;

import com.example.task.service.TokenValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final TokenValidationService tokenValidationService;

    public JwtAuthFilter(TokenValidationService tokenValidationService) {
        this.tokenValidationService = tokenValidationService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token != null) {
            Map<String, Object> tokenInfo = tokenValidationService.validateAndExtractTokenInfo(token);
            if (tokenInfo != null) {
                log.debug("TokenValidationService returned: {}", tokenInfo);
                Number userIdNumber = (Number) tokenInfo.get("userId");
                Long userId = userIdNumber.longValue();
                String role = (String) tokenInfo.get("role");
                log.debug("Extracted userId: {}, role: {} from token validation service", userId, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                log.debug("Setting authentication with role: ROLE_{}", role);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            log.debug("Token validation failed or auth service unavailable, continuing without authentication.");
        }
        filterChain.doFilter(request, response);
    }
}