package giuliaciampa.YouRoster.runners;

import giuliaciampa.YouRoster.services.AccountService;
import giuliaciampa.YouRoster.services.AuthService;
import giuliaciampa.YouRoster.services.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RolesInitializer implements CommandLineRunner {
    //ATTRIBUTI
    private final RoleService roleService;


    //COSTRUTTORE
    public RolesInitializer(RoleService roleService, AuthService authService, AccountService accountService) {
        this.roleService = roleService;
        
    }

    @Override
    public void run(String... args) throws Exception {

        //POPOLA IL DB CON I RUOLI
        roleService.saveRoles();
        System.out.println("Ruoli inseriti all'avvio");


    }
}
