package com.mycompany.clientservice.service;
import com.mycompany.clientservice.exception.ClientNotFoundException;
import com.mycompany.clientservice.exception.UsernameAlreadyExistsException;
import com.mycompany.clientservice.model.dto.AppUserDTO;
import com.mycompany.clientservice.model.dto.AuthRequest;
import com.mycompany.clientservice.model.dto.AuthResponse;
import com.mycompany.clientservice.model.dto.RegisterRequest;
import com.mycompany.clientservice.exception.JwtAuthException;
import com.mycompany.clientservice.exception.RoleNotFoundException;
import com.mycompany.clientservice.entity.AppUser;
import com.mycompany.clientservice.entity.Role;
import com.mycompany.clientservice.repository.RoleRepository;
import com.mycompany.clientservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    public AuthResponse authenticate(AuthRequest authRequest) {
        log.debug("Autenticando usuario: {}", authRequest.getUsername());

        try {
            // Autenticación con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Guardar refreshToken en Redis
            redisTemplate.opsForValue().set(
                    "refresh:" + userDetails.getUsername(),
                    refreshToken,
                    7,
                    TimeUnit.DAYS
            );

            log.info("Usuario autenticado correctamente: {}", authRequest.getUsername());

            return AuthResponse.builder()
                    .token(accessToken)
                    .username(userDetails.getUsername())
                    .roles(userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .refreshToken(refreshToken)
                    .expiresAt(jwtService.getExpirationTime())
                    .build();

        } catch (JwtAuthException e) {
            log.warn("Fallo de autenticación para usuario {}: {}", authRequest.getUsername(), e.getMessage());
            throw new JwtAuthException("Usuario o contraseña incorrectos");
        }
    }

    @Transactional
    public AppUserDTO registerUser(RegisterRequest request) throws RoleNotFoundException {
        log.debug("Registrando nuevo usuario: {}", request.getUsername());

        // Verificar si el username ya existe
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("El username ya está en uso");
        }

        // Si no se envía rol, asignamos uno por defecto
        String roleName = request.getRole() != null ? request.getRole() : "ROLE_USER";

        // Buscar el rol en la DB
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role no encontrado: " + roleName));

        // Construir el usuario con rol asignado
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) //encriptación
                .enabled(true)
                .roles(Set.of(role))
                .build();

        AppUser savedUser = userRepository.save(user);

        log.info("Usuario registrado correctamente: {}", savedUser.getUsername());

        return AppUserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .enabled(savedUser.isEnabled())
                .roles(savedUser.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }
}
