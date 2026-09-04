package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.responses.RoleResponseDTO;
import giuliaciampa.YouRoster.entities.Role;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;


    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

    }


    //1. METODO PER SALVARE I RUOLI SE NON ESISTONO GIA'
    public void saveRoles() {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role("ADMIN"));
        }

        if (roleRepository.findByName("MANAGER").isEmpty()) {
            roleRepository.save(new Role("MANAGER"));
        }

        if (roleRepository.findByName("SHIFT MANAGER").isEmpty()) {
            roleRepository.save(new Role("SHIFT MANAGER"));
        }
        if (roleRepository.findByName("HR").isEmpty()) {
            roleRepository.save(new Role("HR"));
        }
        if (roleRepository.findByName("AP E PAYROLL SPECIALIST").isEmpty()) {
            roleRepository.save(new Role("AP E PAYROLL SPECIALIST"));
        }

        if (roleRepository.findByName("COORDINATOR").isEmpty()) {
            roleRepository.save(new Role("COORDINATOR"));
        }

        if (roleRepository.findByName("STAFF").isEmpty()) {
            roleRepository.save(new Role("STAFF"));
        }
        
    }

    //2. METODO PER CERCARE UN RUOLO PER NOME, DA USARE PER ASSEGNARLO
    public Role findRoleByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() -> new NotFoundException("Il ruolo con il nome " + name + " non è stato trovato."));
    }


    //3 GET TUTTI I RUOLI
    public List<RoleResponseDTO> findAll() {

        List<Role> allRoles = roleRepository.findAll();
        return allRoles.stream().map(role -> new RoleResponseDTO(role.getId(), role.getName())).toList();
    }


}
