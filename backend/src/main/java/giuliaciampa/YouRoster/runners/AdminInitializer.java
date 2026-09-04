package giuliaciampa.YouRoster.runners;

import giuliaciampa.YouRoster.entities.AccountStatus;
import giuliaciampa.YouRoster.services.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    public final AccountService accountService;
    private final String password;
    private final String email;

    public AdminInitializer(AccountService accountService, @Value("${password.admin}") String password, @Value("${email.admin}") String email) {
        this.accountService = accountService;
        this.password = password;
        this.email = email;
    }

    @Override
    public void run(String... args) throws Exception {
        accountService.saveAdmin(email, password, AccountStatus.ACTIVE);
        System.out.println("inizializzazione admin completata");
    }
}
