package giuliaciampa.YouRoster.services;

import com.cloudinary.Cloudinary;
import giuliaciampa.YouRoster.dto.requests.LoginRequestDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateCredentialsRequestDTO;
import giuliaciampa.YouRoster.dto.requests.UserRegistrationRequestDTO;
import giuliaciampa.YouRoster.dto.responses.LoginResponseDTO;
import giuliaciampa.YouRoster.dto.responses.UpdateCredentialsResponseDTO;
import giuliaciampa.YouRoster.dto.responses.UserRegistrationResponseDTO;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.security.JWTTools;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {
    private final AccountService accountService;
    private final UserService userService;
    private final PasswordEncoder bcrypt;
    private final JWTTools jwtTools;
    private final RefreshTokenService refreshTokenService;


    public AuthService(AccountService accountService, UserService userService, RoleService roleService, PasswordEncoder bcrypt, JWTTools jwtTools, RefreshTokenService refreshTokenService, Cloudinary cloudinary) {
        this.accountService = accountService;
        this.userService = userService;
        this.bcrypt = bcrypt;
        this.jwtTools = jwtTools;
        this.refreshTokenService = refreshTokenService;
    }


    //METODO SALVA NUOVO ACCOUNT-REGISTER E CREA UNO USER
    @Transactional
    public UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO payload) {

        // 1. Controlli sui dati già esistenti nel DB
        String correctEmail = accountService.checkIfEmailAlreadyExists(payload.email());
        String correctTaxCode = userService.checkIfTaxCodeAlreadyExists(payload.taxCode());
        String correctDocumentNumber = userService.checkIfDocumentNumberAlreadyExists(payload.documentNumber());

        //2. controllo uguaglianza password
        if (!payload.password().equals(payload.confirmPassword())) {
            throw new ValidationException("Le password inserite non coincidono, riprova.");
        }

        //3. Controllo formato documento in base alla nazionalità
        String cleanNumber = payload.documentNumber().trim().toUpperCase();
        boolean isValid;

        if ("IT".equalsIgnoreCase(payload.nationality().trim()) || "ITALIA".equalsIgnoreCase(payload.nationality().trim())) {
            isValid = switch (payload.documentType()) {
                case IDENTITY_CARD -> cleanNumber.matches("^[A-Z]{2}[0-9]{5}[A-Z]{2}$");
                case DRIVING_LICENSE -> cleanNumber.matches("^[A-Z]{2}[0-9]{7}[A-Z]{1}$");
                case PASSPORT -> cleanNumber.matches("^[A-Z0-9]{5,20}$");
            };
        } else {
            isValid = cleanNumber.matches("^[A-Z0-9]{5,20}$");
        }

        if (!isValid) {
            throw new ValidationException("Il formato del numero di documento non è valido per la nazionalità " + payload.nationality());
        }

        //UPLOAD DEI DOCUMENTI TRAMITE USER SERVICE
        String documentFrontUrl = userService.uploadDocuments(payload.documentFront(), "Documento Fronte");
        String documentBackUrl = userService.uploadDocuments(payload.documentBack(), "Documento Retro");
        String taxCodeCardFrontUrl = userService.uploadDocuments(payload.taxCodeFront(), "Fronte codice fiscale");
        String taxCodeCardBackUrl = userService.uploadDocuments(payload.taxCodeBack(), "Retro codice fiscale");


        // GESTIONE FOTO PROFILO / AVATAR CON FALLBACK
        String photoUrl;
        if (payload.avatar() != null && !payload.avatar().isEmpty()) {
            // Se l'utente ha caricato una foto, la carichi con il servizio
            photoUrl = userService.uploadDocuments(payload.avatar(), "Foto Profilo");
        } else {
            // Altrimenti generi l'avatar di default con le iniziali
            photoUrl = "https://ui-avatars.com/api/?name="
                    + payload.name() + "+" + payload.surname()
                    + "&background=E35226&color=FFFFFF&rounded=true&bold=true&size=128&format=svg";
        }
        //4. Salva Account
        Account savedAccount = accountService.saveAccount(correctEmail, payload.password(), null, AccountStatus.PENDING);


        //6. Crea e salva lo User associato
        User user = new User();
        user.setName(payload.name());
        user.setSurname(payload.surname());
        user.setNationality(payload.nationality().trim().toUpperCase());
        user.setTaxCode(correctTaxCode);
        user.setDateOfBirth(payload.dateOfBirth());
        user.setPlaceOfBirth(payload.placeOfBirth());
        user.setPhoneNumber(payload.phoneNumber());
        user.setStreetAddress(payload.streetAddress());
        user.setHouseNumber(payload.houseNumber());
        user.setZipCode(payload.zipCode());
        user.setCity(payload.city());
        user.setProvince(payload.province());
        user.setPhotoUrl(photoUrl);
        user.setIban(payload.iban().trim().toUpperCase());
        user.setDocumentNumber(correctDocumentNumber);
        user.setDocumentType(payload.documentType());
        user.setIssueDate(payload.issueDate());
        user.setExpirationDate(payload.expirationDate());
        user.setDocumentFrontUrl(documentFrontUrl);
        user.setDocumentBackUrl(documentBackUrl);
        user.setTaxCodeCardFrontUrl(taxCodeCardFrontUrl);
        user.setTaxCodeCardBackUrl(taxCodeCardBackUrl);
        user.setAccount(savedAccount);
        savedAccount.setUser(user);


        User savedUser = userService.saveUser(user);
        // 6. Return DTO
        return new UserRegistrationResponseDTO(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getSurname(),
                savedUser.getAccount().getEmail(),
                "Registrazione avvenuta con successo, presto l'amministratore potrà visualizzare la tua richiesta di attivazione.",
                LocalDateTime.now()
        );
    }


    //LOGIN
    public LoginResponseDTO login(LoginRequestDTO payload) {
        //1. cerca account per email
        Account account = accountService.findAccountByEmail(payload.email());

        //2. verifica la password
        if (!bcrypt.matches(payload.password(), account.getPassword())) {
            throw new UnauthorizedException("Credenziali non valide");
        }

        //3. verifica lo stato dell'account
        switch (account.getStatus()) {
            case PENDING ->
                    throw new UnauthorizedException("Il tuo account è ancora in attesa di approvazione da parte dell'amministratore.");
            case DISABLED ->
                    throw new UnauthorizedException("Il tuo account è stato disabilitato. Contatta l'amministratore per la riattivazione.");
            case REJECTED -> throw new UnauthorizedException("La tua richiesta di registrazione è stata rifiutata.");
            case ACTIVE -> { /* Procedi con il login */ }
        }

        //4. genera il token
        String accessToken = jwtTools.generateToken(account);

        // 5. Genera - aggiorna il Refresh Token nel database
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(account);

        //6. Gestisci la photoUrl
        boolean isAdmin = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        String roleName = account.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Non hai i permessi per accedere"));

        String photoUrl;
        if (isAdmin) {
            // L'admin non ha user, genero foto al volo
            photoUrl = "https://ui-avatars.com/api/?name=Admin&background=9E4289&color=E3E3CA&rounded=true&bold=true&size=150&font-size=0.2&format=svg&length=5";
        } else {
            // L'utente normale ha già la sua foto salvata nel DB con la registrazione
            photoUrl = photoUrl = "https://ui-avatars.com/api/?name=" + account.getUser().getName() + "+" + account.getUser().getSurname() + "&background=dc5031&color=F7F5DE&rounded=true&bold=true&size=150&font-size=0.4&format=svg";
        }


        return new LoginResponseDTO(accessToken, refreshToken.getToken(), photoUrl, roleName);
    }


    //AGGIORNA CREDENZIALI ADMIN/UTENTE LOGGATO
    @Transactional
    public UpdateCredentialsResponseDTO updateCredentials(UUID accountId, UpdateCredentialsRequestDTO payload) {
        Account account = accountService.findById(accountId);

        boolean emailChanged = false;
        boolean passwordChanged = false;

        //1. se l'email cambia, controlla che payload.email non sia null e sia diversa da quella attuale
        if (payload.email() != null && !account.getEmail().equalsIgnoreCase(payload.email())) {
            //controlla che non sia già in uso
            accountService.checkIfEmailAlreadyExists(payload.email());
            account.setEmail(payload.email());
            emailChanged = true;
        }

        //2. bcrypt e aggiornamento della password, solo se viene fornita una nuova password
        if (payload.newPassword() != null && !payload.newPassword().isBlank()) {

            if (payload.oldPassword() == null || !bcrypt.matches(payload.oldPassword(), account.getPassword())) {
                throw new BadRequestException("La vecchia password non è corretta o non è stata inserita.");
            }

            if (!Objects.equals(payload.newPassword(), (payload.confirmNewPassword()))) {
                throw new BadRequestException("La nuova password e la conferma password non coincidono.");
            }
            account.setPassword(bcrypt.encode(payload.newPassword()));
            passwordChanged = true;
        }


        accountService.save(account);

        // 3. Risposta dinamica in base alle modifiche effettive
        if (emailChanged && passwordChanged) {
            return new UpdateCredentialsResponseDTO("Email e password aggiornate con successo.", LocalDateTime.now());
        } else if (emailChanged) {
            return new UpdateCredentialsResponseDTO("Email aggiornata con successo.", LocalDateTime.now());
        } else if (passwordChanged) {
            return new UpdateCredentialsResponseDTO("Password aggiornata con successo.", LocalDateTime.now());
        }

        return new UpdateCredentialsResponseDTO("Nessuna modifica effettuata.", LocalDateTime.now());
    }


    //METODO RINNOVO REFRESH TOKEN
    @Transactional
    public LoginResponseDTO refreshToken(String requestFreshToken) {
        //1. cerca il token nel db
        RefreshToken token = refreshTokenService.findByToken(requestFreshToken);

        //2. verifica che non sia scaduto
        refreshTokenService.verifyExpiration(token);

        Account account = token.getAccount();

        //2b verifica che l'account sia ancora attivo
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("L'account non è attivo o risulta disabilitato.");
        }

        //3. genera un NUOVO access token
        String newAccessToken = jwtTools.generateToken(account);

        //4. genera un NUOVO refresh token(refresh token rotation)
        RefreshToken newRefreshToken = refreshTokenService.generateRefreshToken(account);


        boolean isAdmin = account.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        String photoUrl;
        if (isAdmin) {
            // L'admin non ha user, genero foto al volo
            photoUrl = "https://ui-avatars.com/api/?name=Admin&background=9E4289&color=E3E3CA&rounded=true&bold=true&size=150&format=svg";
        } else {
            // L'utente normale ha già la sua foto salvata nel DB con la registrazione
            photoUrl = account.getUser().getPhotoUrl();
        }


        String roleName = account.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Non hai i permessi per accedere"));

        return new LoginResponseDTO(newAccessToken, newRefreshToken.getToken(), photoUrl, roleName);
    }

}
