package com.mycompany.clientservice.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AuthResponse implements Serializable {
    private String token;
    private String refreshToken;
    private String username;
    private List<String> roles;
    private LocalDateTime expiresAt;
}
