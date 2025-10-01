package com.mycompany.clientservice.mapper;
import com.mycompany.clientservice.model.dto.AppUserDTO;
import com.mycompany.clientservice.entity.AppUser;
import com.mycompany.clientservice.entity.Role;
import java.io.Serializable;
import java.util.stream.Collectors;

public final class AppUserMapper implements Serializable {

    private AppUserMapper() {
    }

    public static AppUserDTO toDTO(AppUser user) {
        if (user == null) return null;

        return AppUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
