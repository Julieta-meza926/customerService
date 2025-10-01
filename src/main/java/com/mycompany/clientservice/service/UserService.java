package com.mycompany.clientservice.service;
import com.mycompany.clientservice.exception.DataIntegrityException;
import com.mycompany.clientservice.exception.RoleNotFoundException;
import com.mycompany.clientservice.exception.UserNotFoundException;
import com.mycompany.clientservice.mapper.AppUserMapper;
import com.mycompany.clientservice.entity.AppUser;
import com.mycompany.clientservice.model.dto.AppUserDTO;
import com.mycompany.clientservice.model.dto.RegisterRequest;
import com.mycompany.clientservice.entity.Role;
import com.mycompany.clientservice.repository.RoleRepository;
import com.mycompany.clientservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;

    @Transactional
    public AppUserDTO registerUser(RegisterRequest request) {
        String roleName = request.getRole() != null ? request.getRole() : "ROLE_USER";

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role no encontrado: " + roleName));

        try {
            AppUser savedUser = userRepository.save(AppUser.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .enabled(true)
                    .roles(Set.of(role))
                    .build());

            log.info("Usuario registrado: {}", savedUser.getUsername());
            return AppUserMapper.toDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException("No se pudo registrar el usuario. Username duplicado.");
        }

}

    @Transactional
    public AppUserDTO addRoleToUser(String username, String roleName) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role no encontrado: " + roleName));

        if (user.getRoles().add(role)) {
            userRepository.save(user);
            log.info("Rol {} agregado al usuario {}", roleName, username);
        }

        return AppUserMapper.toDTO(user); // Usamos el mapper centralizado
    }
}