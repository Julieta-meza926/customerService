package com.mycompany.clientservice.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.clientservice.model.dto.ErrorResponse;
import com.mycompany.clientservice.exception.JwtAuthException;
import com.mycompany.clientservice.service.CustomUserDetailsService;
import com.mycompany.clientservice.service.JwtService;
import com.mycompany.clientservice.service.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (tokenBlacklistService.isTokenRevoked(token)) {
                throw new JwtAuthException("Token revocado");
            }

            String username = jwtService.getUsernameFromToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (!jwtService.isTokenValid(token, userDetails)) {
                    throw new JwtAuthException("Token inválido o expirado");
                }

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (JwtAuthException | JwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .error(ex.getMessage())
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();
            new ObjectMapper().writeValue(response.getWriter(), error);
        }
    }}
