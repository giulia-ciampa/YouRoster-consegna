package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.PasswordResetToken;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.repositories.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountService accountService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    //LINK
    @Value("${resetPasswordToken.url}")
    private String resetPasswordToken;

    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository, AccountService accountService, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.accountService = accountService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void requestPasswordReset(String email) {
        //1. ricerca account per email
        Account account = accountService.findAccountByEmail(email);

        //2. cerchiamo se esiste già un token per l'account nel db altrimenti ne creiamo uno nuovo
        PasswordResetToken resetToken = passwordResetTokenRepository.findByAccount(account)
                .orElse(new PasswordResetToken());

        // 2. Impostiamo o aggiorniamo i campi necessari
        resetToken.setAccount(account);
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        // 3. Salviamo (fare un UPDATE se già esisteva o un INSERT se è nuovo)
        passwordResetTokenRepository.save(resetToken);


        //costruzione link
        String passwordLink = resetPasswordToken + resetToken.getToken();

        // ... invio email con il link ...

        boolean isAdmin = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        String nameToDisplay = isAdmin ? "Admin" : account.getUser().getName();
        String surnameToDisplay = isAdmin ? "" : account.getUser().getSurname();

        String htmlBody = EmailTemplateBuilder.buildResetPassword(nameToDisplay, surnameToDisplay, passwordLink);
        emailService.sendHtmlEmail(account.getEmail(),
                "[Richiesta cambio password - YouRoster!]",
                htmlBody);

    }


    public void resetPassword(String token, String newPassword) {

        // 1. Cerca il token nel database
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Link non valido o scaduto. Procedi richiedendo una nuova email di recupero."));

        // 2. Verifica se il token è scaduto (più di 15 minuti fa)
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Opzionale: puliamo il token scaduto dal db
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Link non valido o scaduto. Procedi richiedendo una nuova email di recupero.");
        }

        // 3. Prendi l'account associato e aggiorna la password criptata
        Account account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(newPassword));
        accountService.save(account);

        // 4. Elimina il token usato (così non può essere riutilizzato)
        passwordResetTokenRepository.delete(resetToken);
    }


}


