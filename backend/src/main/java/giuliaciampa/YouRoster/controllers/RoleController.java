package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.responses.RoleResponseDTO;
import giuliaciampa.YouRoster.services.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;


    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR')")
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.findAll();
    }
}
