package com.mycompany.clientservice.controller;
import com.mycompany.clientservice.model.dto.AuthResponse;
import com.mycompany.clientservice.model.dto.AppUserDTO;
import com.mycompany.clientservice.exception.JwtAuthException;
import com.mycompany.clientservice.service.AuthService;
import com.mycompany.clientservice.model.dto.AuthRequest;
import com.mycompany.clientservice.model.dto.RegisterRequest;
import com.mycompany.clientservice.service.JwtService;
import com.mycompany.clientservice.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthRequest authRequest) {
        log.info("Intento de login para usuario: {}", authRequest.getUsername());
        AuthResponse response = authService.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/session")
    public ResponseEntity<AppUserDTO> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Registro de nuevo usuario: {}", request.getUsername());
        AppUserDTO userDTO = authService.registerUser(request);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/user")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String username = jwtService.getUsernameFromToken(refreshToken);

        String storedToken = redisTemplate.opsForValue().get("refresh:" + username);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new JwtAuthException("Refresh token inválido o expirado");
        }

        UserDetails user = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtService.generateAccessToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", refreshToken);
        return ResponseEntity.ok(tokens);
    }
    @PostMapping("/sessions")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtAuthException("No se proporcionó token para logout");
        }

        String token = authHeader.substring(7);
        long remainingMillis = jwtService.getRemainingMillis(token);
        tokenBlacklistService.revokeToken(token, remainingMillis);

        return ResponseEntity.ok(Map.of("message", "Logout exitoso, token revocado"));
    }
}