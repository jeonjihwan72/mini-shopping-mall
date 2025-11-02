package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.Role;

@Getter
@Setter
public class RoleUpdateRequest {
    private String username;
    private Role newRole;
}
