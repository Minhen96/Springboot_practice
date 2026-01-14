package com.example.mhpractice.common.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;

        // Get the token from the cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                log.debug("Cookie found: {} = {}", cookie.getName(),
                        cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "...");
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        } else {
            log.debug("No cookies in request for {}", request.getRequestURI());
        }

        if (token != null) {
            log.debug("Token found, validating...");
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                log.debug("Token valid for user: {}", email);
                // Create authentication token, later can use it to get user details
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,
                        null, Collections.emptyList());
                // Set token into security context, so that it can be used by other components
                // This security context is not globally, is for per thread
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                log.debug("Token validation failed");
            }
        } else {
            log.debug("No auth_token cookie found for {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
