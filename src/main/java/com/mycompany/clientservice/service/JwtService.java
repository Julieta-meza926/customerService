package com.mycompany.clientservice.service;
import com.mycompany.clientservice.exception.JwtAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.time.Instant;
import java.time.ZoneId;
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.time.expiration}")
    private long accessTokenExpirationMillis;

    // Tiempo fijo de refresh (7 días)
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    // Generación de tokens
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), new HashMap<>(), accessTokenExpirationMillis);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails.getUsername(), new HashMap<>(), REFRESH_TOKEN_EXPIRATION);
    }

    private String buildToken(String username, Map<String, Object> claims, long durationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + durationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validación de tokens
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthException("Token inválido o expirado: " + e.getMessage());
        }
    }

    // Claims helpers
    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaims(token));
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    // Utilidades extras
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public LocalDateTime getExpirationTime() {
        return Instant.now()
                .plusMillis(accessTokenExpirationMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public long getRemainingMillis(String token) {
        return getClaim(token, Claims::getExpiration).getTime() - System.currentTimeMillis();
    }
}
