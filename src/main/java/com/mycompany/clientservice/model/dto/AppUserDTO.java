package com.mycompany.clientservice.model.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDTO implements Serializable {
    private Long id;
    private String username;
    private boolean enabled;
    private Set<String> roles;
}
