package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.AdminApprovalRequestDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateAccountRoleDTO;
import giuliaciampa.YouRoster.dto.responses.AccountSummaryDTO;
import giuliaciampa.YouRoster.dto.responses.MessageResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.AlreadyExistsException;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.repositories.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder bcrypt;
    private final RoleService roleService;
    private final OfficeService officeService;
    private final EmailService emailService;

    @Value("${login.url}")
    private String loginUrl;

    @Value("${roleUpdated.url}")
    private String roleUpdated;

    public AccountService(AccountRepository accountRepository, PasswordEncoder bcrypt, RoleService roleService, OfficeService officeService, EmailService emailService) {
        this.accountRepository = accountRepository;
        this.bcrypt = bcrypt;
        this.roleService = roleService;
        this.officeService = officeService;
        this.emailService = emailService;
    }


    //HELPER, CONVERTE ACCOUNT IN ACCOUNTSUMMARYDTO
    private AccountSummaryDTO convertToAccountSummaryDTO(Account account) {
        User user = account.getUser();

        Set<String> roleNames = (account.getRoles() != null)
                ? account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet())
                : Set.of();

        if (user == null) {
            return new AccountSummaryDTO(account.getId(), "Utente di sistema", null, null, null, "Nessuna sede", account.getEmail(), account.getStatus(), roleNames);
        }

        String officeName = (user.getReferenceOffice() != null) ? user.getReferenceOffice().getName() : "Nessuna sede";

        return new AccountSummaryDTO(
                account.getId(),
                user.getName(),
                user.getSurname(),
                user.getPhoneNumber(),
                user.getPhotoUrl(),
                officeName,
                account.getEmail(),
                account.getStatus(),
                roleNames
        );
    }

    //CREA E SALVA NUOVO ACCOUNT(METODO PER LA REGISTRAZIONE)
    public Account saveAccount(String email, String password, Set<Role> roles, AccountStatus accountStatus) {
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(bcrypt.encode(password));
        account.setStatus(accountStatus);


        if (roles != null && !roles.isEmpty()) {
            account.setRoles(roles);
        } else {
            account.setRoles(new HashSet<>());
        }
        return accountRepository.save(account);
    }


    // CREA ACCOUNT PER L'ADMIN SE NON ESISTE(RUNNER)
    public void saveAdmin(String defaultEmail, String defaultPassword, AccountStatus status) {
        boolean adminExist = accountRepository.existsByRoles_Name("ADMIN");


        if (!adminExist) {
            System.out.println("Nessun Admin trovato. Creazione Admin di default in corso...");

            Role adminRole = roleService.findRoleByName("ADMIN");
            saveAccount(defaultEmail, defaultPassword, Set.of(adminRole), AccountStatus.ACTIVE);
            System.out.println("Admin creato con successo!");
        } else {
            System.out.println("Account ADMIN già presente nel sistema.");
        }
    }

    // FIND BY ID
    public Account findById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("L'account con id " + id + " non è stato trovato"));
    }

    // METODO TROVA ACCOUNT PER EMAIL (PER LOGIN)
    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("L'email " + email + " non esiste nel nostro sistema"));
    }

    // METODO CONTROLLA ESISTENZA EMAIL(PER REGISTRAZIONE)
    public String checkIfEmailAlreadyExists(String email) {
        if (accountRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("L'utente con email " + email + " è già esistente");
        }
        return email;
    }

    // SALVA ACCOUNT PER AGGIORNAMENTO CREDENZIALI
    public void save(Account account) {
        accountRepository.save(account);
    }

    //RECUPERA EMAIL IN BASE AI RUOLI
    public List<String> getEmailsByRoles(List<String> roleNames) {
        List<Account> accounts = accountRepository.findByRoles_NameIn(roleNames);

        return accounts.stream()
                .map(Account::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();
    }

    //FIND BY ROLE NAME
    public List<Account> findByRolesNameIn(List<String> roleNames) {
        return accountRepository.findByRolesNameIn(roleNames);
    }


    //-------------------------------------------------------------------------------------------------------

    // 1. TROVA GLI ACCOUNT IN ATTESA DI ESSERE ACCETTATI(PENDING)
    public Page<AccountSummaryDTO> getPendingAccounts(int page, int size, String sortBy, Sort.Direction direction) {
        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Page<Account> accountsPage = accountRepository.findByStatus(AccountStatus.PENDING, pageable);

        return accountsPage.map(this::convertToAccountSummaryDTO);
    }

    // 2. APPROVA E ASSEGNA RUOLO + SEDE (ADMIN)
    @Transactional
    public MessageResponseDTO approveAssignRolesAndOffice(UUID id, AdminApprovalRequestDTO payload) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("L'account con id " + id + " non è stato trovato"));

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("L'account è già stato approvato ed è attivo.");
        }

        // Estrazione sicura delle proprietà dal payload
        UUID officeId = (payload != null) ? payload.officeId() : null;

        // 1. Mappatura dei ruoli (default STAFF se vuoto)
        Set<Role> rolesToAssign = new HashSet<>();
        if (payload == null || payload.roles() == null || payload.roles().isEmpty()) {
            Role defaultRole = roleService.findRoleByName("STAFF");
            rolesToAssign.add(defaultRole);
        } else {
            rolesToAssign = payload.roles().stream()
                    .map(roleService::findRoleByName)
                    .collect(Collectors.toSet());
        }

        if (account.getRoles() == null) {
            account.setRoles(new HashSet<>());
        }
        account.getRoles().clear();
        account.getRoles().addAll(rolesToAssign);

        // 2. Recupera lo user
        User user = account.getUser();
        String userName = (user != null) ? user.getName() : account.getEmail();
        String userSurname = (user != null) ? user.getSurname() : "Utente del sistema";


        // 3. Verifica ruoli
        boolean isCoordinator = rolesToAssign.stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("COORDINATOR"));

        // 4. Gestione Sede
        Office officeToAssign = null;
        if (isCoordinator) {
            if (officeId == null) {
                throw new BadRequestException("Il coordinatore " + userName + " " + userSurname + " deve avere una sede di riferimento");
            }
            officeToAssign = officeService.findById(officeId);
        } else if (officeId != null) {
            officeToAssign = officeService.findById(officeId);
        }

        String officeName = (officeToAssign != null) ? officeToAssign.getName() : "Nessun ufficio/sede assegnata";


        // 6. Assegnazione Sede allo User
        if (user != null) {
            user.setReferenceOffice(officeToAssign);
        }

        // 5. Attivazione e salvataggio account
        account.setStatus(AccountStatus.ACTIVE);
        account.setRoles(rolesToAssign);
        accountRepository.save(account);

        String roleNames = rolesToAssign.stream()
                .map(Role::getName)
                .collect(Collectors.joining(", "));

        // 7. Invio email
        String htmlBody = EmailTemplateBuilder.buildAccountApprovalEmail(
                userName,
                roleNames,
                officeToAssign != null ? officeToAssign.getName() : "Al momento non è stata assegnata nessuna sede specifica",
                loginUrl
        );

        emailService.sendHtmlEmail(
                account.getEmail(),
                "[Account Approvato - Benvenuto in YouRoster!]",
                htmlBody
        );

        if (isCoordinator && officeToAssign != null) {
            return new MessageResponseDTO("L'account di " + userName + " " + userSurname + " è stato attivato con successo con ruolo " + roleNames + " nella sede " + officeToAssign.getName(), LocalDateTime.now());
        }

        return new MessageResponseDTO("L'account di " + userName + " " + userSurname + " è stato attivato con successo con ruolo " + roleNames, LocalDateTime.now());
    }


    // 3. RIFIUTA E ELIMINA RICHIESTA (ADMIN)
    @Transactional
    public MessageResponseDTO rejectAccount(UUID id) {
        Account accountFound = findById(id);

        //1. Controllo di sicurezza: non si può rifiutare un account già attivo
        if (accountFound.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("Impossibile rifiutare: l'account è già attivo.");
        }

        User user = accountFound.getUser();
        String userName = (user != null) ? user.getName() : accountFound.getEmail();
        String userSurname = (user != null) ? user.getSurname() : "Utente del sistema";

        //2. invio email di notifica prima della cancellazione dei dati
        String htmlBody = EmailTemplateBuilder.buildAccountRejectionEmail(accountFound.getEmail());
        emailService.sendHtmlEmail(
                accountFound.getEmail(),
                "[Esito Richiesta Registrazione - YouRoster]",
                htmlBody
        );

        // 3. Cancellazione dal DB

        accountRepository.delete(accountFound);

        // 4. Ritorna il DTO di risposta
        return new MessageResponseDTO(
                "La richiesta di registrazione dell'utente " + userName + " " + userSurname + " è stata rifiutata e rimossa dal sistema.",
                LocalDateTime.now()
        );
    }

    // 4. DISABILITA ACCOUNT (ADMIN)
    @Transactional
    public MessageResponseDTO disableAccount(UUID accountId) {
        Account foundAccount = findById(accountId);
        User foundUser = foundAccount.getUser();

        String userName = (foundUser != null) ? foundUser.getName() : foundAccount.getEmail();
        String userSurname = (foundUser != null) ? foundUser.getSurname() : "Utente del sistema";

        if (foundAccount.getStatus() == AccountStatus.DISABLED) {
            throw new BadRequestException("L'account appartenente a " + userName + " " + userSurname + " è già disabilitato");
        }

        foundAccount.setStatus(AccountStatus.DISABLED);
        accountRepository.save(foundAccount);

        // Email avviso account disabilitato
        String htmlBody = EmailTemplateBuilder.buildAccountDisableEmail(userName);

        emailService.sendHtmlEmail(
                foundAccount.getEmail(),
                "[Disattivazione Account - YouRoster]",
                htmlBody
        );

        return new MessageResponseDTO("L'account appartenente a " + userName + " " + userSurname + " è stato disabilitato con successo", LocalDateTime.now());
    }

    // 5. DATO UN RUOLO, TROVA GLI ACCOUNT CON QUEL RUOLO
    public Page<AccountSummaryDTO> getAccountByRole(String roleName, Pageable pageable) {
        Page<Account> accountsPage = accountRepository.findByRolesNameIgnoreCaseAndStatus(roleName, AccountStatus.ACTIVE, pageable);
        return accountsPage.map(this::convertToAccountSummaryDTO);
    }

    // 6. TROVA GLI ACCOUNT DISABILITATI
    public Page<AccountSummaryDTO> getSuspendedAccount(Pageable pageable) {
        Page<Account> suspendedAccounts = accountRepository.findByStatus(AccountStatus.DISABLED, pageable);
        return suspendedAccounts.map(this::convertToAccountSummaryDTO);
    }

    //7. RIATTIVA UN ACCOUNT DISABILITATO
    @Transactional
    public MessageResponseDTO reactivateSuspendedAccount(UUID accountId, String roleName) {
        Account account = findById(accountId);
        User user = account.getUser();

        String userName = (user != null) ? user.getName() : account.getEmail();
        String userSurname = (user != null) ? user.getSurname() : "Utente del sistema";

        //1. verifica che l'account sia effettivamente disattivato
        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BadRequestException("L'account " + userName + " " + userSurname + " è già attivo.");
        }

        // 2. quando admin riattiva, se non dà ruolo è staff di default
        String targetRoleName = (roleName != null && !roleName.isBlank()) ? roleName : "STAFF";
        Role newRole = roleService.findRoleByName(targetRoleName);

        //3. attivo l'account e imposto il ruolo
        account.setStatus(AccountStatus.ACTIVE);

        Set<Role> updatedRoles = new HashSet<>();
        updatedRoles.add(newRole);
        account.setRoles(updatedRoles);

        accountRepository.save(account);

        // 4. Invio email di notifica riattivazione
        String officeName = (user != null && user.getReferenceOffice() != null)
                ? user.getReferenceOffice().getName()
                : "Non è stato assegnato nessun ufficio di riferimento";

        String htmlBody = EmailTemplateBuilder.buildAccountApprovalEmail(
                userName,
                newRole.getName(),
                officeName,
                loginUrl
        );

        emailService.sendHtmlEmail(
                account.getEmail(),
                "[Account Riattivato - YouRoster]",
                htmlBody
        );


        return new MessageResponseDTO(
                "L'account di " + userName + " " + userSurname + " è stato riattivato con successo con ruolo " + newRole.getName(),
                LocalDateTime.now()
        );
    }


    //8. VISUALIZZARE TUTTI GLI ACCOUNT ATTIVI
    public Page<AccountSummaryDTO> getActiveAccounts(Pageable pageable) {
        Page<Account> activeAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE, pageable);

        return activeAccounts.map(account -> {
            User user = account.getUser();


            String name = (user != null) ? user.getName() : null;
            String surname = (user != null) ? user.getSurname() : null;

            String phoneNumber = (user != null) ? user.getPhoneNumber() : null;

            String photoUrl = (user != null) ? user.getPhotoUrl() : null;

            String officeName = (user != null && user.getReferenceOffice() != null)
                    ? user.getReferenceOffice().getName()
                    : "Nessuna sede assegnata";
            return convertToAccountSummaryDTO(account);

        });
    }


    //9. CERCA UTENTE ATTIVO PER NOME (TUTTI)
    public Page<AccountSummaryDTO> searchActiveUsersByNameAndSurname(String name, String surname, Pageable pageable) {
        Page<Account> activeAccounts = accountRepository
                .findByUser_NameContainingIgnoreCaseAndUser_SurnameContainingIgnoreCaseAndStatus(
                        name,
                        surname,
                        AccountStatus.ACTIVE,
                        pageable
                );

        return activeAccounts.map(this::convertToAccountSummaryDTO);
    }

    //10. AGGIORNA RUOLI (ADMIN)
    @Transactional
    public MessageResponseDTO updateAccountRole(UpdateAccountRoleDTO payload, UUID id) {
        Account account = findById(id);
        User user = account.getUser();

        Set<Role> rolesToAssign = payload.roles().stream()
                .map(roleService::findRoleByName)
                .collect(Collectors.toSet());

        account.setRoles(rolesToAssign);
        accountRepository.save(account);

        // Conversione ruoli in stringa leggibile per l'email
        String rolesString = rolesToAssign.stream()
                .map(Role::getName)
                .collect(Collectors.joining(", "));

        // Gestione nel caso in cui l'utente associato non esista
        String userName = (user != null) ? "Ciao " + user.getName() : "Gentile utente";
        String recipientName = (user != null)
                ? user.getName() + " " + user.getSurname()
                : account.getEmail();

        //invio email
        String htmlBody = EmailTemplateBuilder.buildUpdateRole(
                userName,
                rolesString,
                roleUpdated
        );

        emailService.sendHtmlEmail(
                account.getEmail(),
                "[Ruolo aggiornato - YouRoster]!",
                htmlBody
        );


        return new MessageResponseDTO("Il ruolo di " + recipientName + " è stato aggiornato con successo. Il suo ruolo attuale è: " + rolesString, LocalDateTime.now());

    }

    //11. TROVA COORDINATORI TRAMITE UFFICIO ASSEGNATO
    public Page<AccountSummaryDTO> getCoordinatorsByOffice(UUID officeId, Pageable pageable) {
        Page<Account> accounts = accountRepository.findCoordinatorsByOffice(AccountStatus.ACTIVE, "COORDINATOR", officeId, pageable);
        return accounts.map(this::convertToAccountSummaryDTO);
    }

}







