package com.mycompany.clientservice.service;
import com.mycompany.clientservice.model.dto.AppUserDTO;
import com.mycompany.clientservice.exception.UserNotFoundException;
import com.mycompany.clientservice.entity.AppUser;
import com.mycompany.clientservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    // devuelve un dto simple(serializable, evita errores de Redis)
    @Cacheable(value = "users", key = "#username")
    public AppUserDTO loadUserByUsernameDTO(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + username));

        return AppUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()))
                .build();
    }
}

