package giuliaciampa.YouRoster.services;

import com.cloudinary.Cloudinary;
import giuliaciampa.YouRoster.dto.requests.UpdateUserProfileDTO;
import giuliaciampa.YouRoster.dto.responses.UserBasicInformationResponseDTO;
import giuliaciampa.YouRoster.dto.responses.UserForShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.responses.UserProfileResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.AlreadyExistsException;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final EmailService emailService;
    private final AccountService accountService;
    private final OfficeService officeService;


    @Value("${profileUpdated.url}")
    private String ProfileUpdated;


    public UserService(UserRepository userRepository, Cloudinary cloudinary, EmailService emailService, AccountService accountService, OfficeService officeService) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
        this.emailService = emailService;
        this.accountService = accountService;
        this.officeService = officeService;
    }

    //find all
    List<User> findAll() {
        return userRepository.findAll();
    }

    //CONTROLLA SE L'UTENTE GIA' ESISTE DAL CODICE FISCALE
    public String checkIfTaxCodeAlreadyExists(String taxCode) {

        String cleanTaxCode = taxCode.trim().toUpperCase();
        if (userRepository.existsByTaxCode(cleanTaxCode)) {
            throw new AlreadyExistsException("L'utente con il codice fiscale " + cleanTaxCode + " è già esistente");
        }

        return cleanTaxCode;
    }

    //CONTROLLA SE L'UTENTE GIA' ESISTE DAL NUMERO DI DOCUMENTO
    public String checkIfDocumentNumberAlreadyExists(String documentNumber) {

        String cleanDocumentNumber = documentNumber.trim().toUpperCase();
        if (userRepository.existsByDocumentNumber(cleanDocumentNumber)) {
            throw new AlreadyExistsException("L'utente con il numero di documento " + cleanDocumentNumber + " è già esistente");
        }


        return cleanDocumentNumber;
    }

    //METODO PER SALVARE LO USER
    public User saveUser(User user) {
        return userRepository.save(user);
    }


    //TROVA LO USER IN BASE ALL'ID DELL'ACCOUNT
    public User findByAccountId(UUID accountId) {
        return userRepository.findByAccount_Id(accountId).orElseThrow(() -> new NotFoundException("l'utente con account id " + accountId + " non è stato trovato"));
    }

    //METODO CARICAMENTO DOCUMENTI
    public String uploadDocuments(MultipartFile file, String documentName) {

        if (file == null || file.isEmpty()) {
            throw new ValidationException("Il caricamento del file per " + documentName + " è obbligatorio");
        }

        //controllo dimensione
        long maxSizeBytes = 6 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new ValidationException("Il file " + documentName + " supera il limite massimo di 6 MB.");
        }

        //controllo tipo file, solo immagini o pdf
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Il nome del file per " + documentName + " non è valido.");
        }

        String lowerName = originalFilename.toLowerCase();

        boolean isValidExtension = lowerName.endsWith(".png") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".pdf");

        if (!isValidExtension) {
            throw new ValidationException("Il file " + documentName + " deve avere un'estensione valida (.png, .jpg, .jpeg, .pdf).");
        }

        // Controllo sul MIME Type per ulteriore sicurezza
        String contentType = file.getContentType();

        boolean isImageExtension = lowerName.endsWith(".png") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".jpg");
        boolean isPdfExtension = lowerName.endsWith(".pdf");

        boolean isValidMime = (contentType != null && (contentType.startsWith("image/") || contentType.equals("application/pdf")));
        boolean isGenericStreamWithValidExtension = (contentType != null && contentType.equals("application/octet-stream") && (isImageExtension || isPdfExtension));

        if (!isValidMime && !isGenericStreamWithValidExtension) {
            throw new ValidationException("Il formato del file " + documentName + " non è supportato.");
        }


        //upload del file su cloudinary
        try {
            Map<String, Object> uploadParams = Map.of(
                    "folder", "documents",
                    "quality", "auto" // Compressione automatica di Cloudinary
            );
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento di " + documentName, e);
        }

    }

    //METODO CARICAMENTO DOCUMENTI PER AGGIORNAMENTO
    public String uploadOptionalDocument(MultipartFile file, String documentName) {

        if (file == null || file.isEmpty()) {
            return null;
        }
        
        //controllo dimensione
        long maxSizeBytes = 6 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new ValidationException("Il file " + documentName + " supera il limite massimo di 6 MB.");
        }

        //controllo tipo file, solo immagini o pdf
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Il nome del file per " + documentName + " non è valido.");
        }

        String lowerName = originalFilename.toLowerCase();

        boolean isValidExtension = lowerName.endsWith(".png") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".pdf");

        if (!isValidExtension) {
            throw new ValidationException("Il file " + documentName + " deve avere un'estensione valida (.png, .jpg, .jpeg, .pdf).");
        }

        // Controllo sul MIME Type per ulteriore sicurezza
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new ValidationException("Il formato del file " + documentName + " non è supportato.");
        }

        //upload del file su cloudinary
        try {
            Map<String, Object> uploadParams = Map.of(
                    "folder", "documents",
                    "quality", "auto" // Compressione automatica di Cloudinary
            );
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento di " + documentName, e);
        }

    }

    //FIND BY ID
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("L'utente con id " + id + " non è stato trovato."));
    }


    //-------------------------------------------------------------------------------------------------------------
    //HELPER
    private UserProfileResponseDTO mapToDTO(User user, Account account) {
        String officeName = (user.getReferenceOffice() != null)
                ? user.getReferenceOffice().getName()
                : "Nessuna sede assegnata";

        Set<String> roleNames = account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        UUID officeId = user.getReferenceOffice() != null
                ? user.getReferenceOffice().getId()
                : null;

        return new UserProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getDateOfBirth(),
                user.getPlaceOfBirth(),
                user.getPhoneNumber(),
                user.getTaxCode(),
                user.getPhotoUrl(),
                user.getStreetAddress(),
                user.getHouseNumber(),
                user.getZipCode(),
                user.getCity(),
                user.getProvince(),
                user.getIban(),
                user.getDocumentNumber(),
                user.getDocumentType(),
                user.getIssueDate(),
                user.getExpirationDate(),
                user.getDocumentFrontUrl(),
                user.getDocumentBackUrl(),
                user.getTaxCodeCardFrontUrl(),
                user.getTaxCodeCardBackUrl(),
                officeName,
                roleNames,
                user.getAccount().getEmail(),
                user.getAccount().getStatus(),
                officeId,
                user.getAccount().getId()

        );
    }


    //1. VISUALIZZA PROPRIO PROFILO
    @Transactional
    public UserProfileResponseDTO getMyProfile(Account currentAccount) {
        if (currentAccount.getUser() == null) {
            throw new NotFoundException("Nessun profilo utente associato a questo account!");
        }

        User user = userRepository.findByIdWithOffice(currentAccount.getUser().getId())
                .orElseThrow(() -> new NotFoundException("Profilo utente non trovato"));


        return mapToDTO(user, currentAccount);

    }

    //2. MODIFICA DATI PROFILO, INVIA EMAIL SE MODIFICA DATI SENSIBILI
    @Transactional
    public UserProfileResponseDTO updateProfile(
            Account currentAccount,
            UpdateUserProfileDTO payload
    ) {
        if (currentAccount.getUser() == null) {
            throw new NotFoundException(
                    "Nessun profilo utente è associato a questo account"
            );
        }

        // 1. Recupero l'utente
        User user = userRepository.findByIdWithOffice(currentAccount.getUser().getId())
                .orElseThrow(() ->
                        new NotFoundException("Profilo utente non trovato")
                );

        // 2. Tracciamento modifiche sensibili
        boolean personalDataChanged = false;
        boolean ibanChanged = false;
        boolean documentDataChanged = false;

        // 3. Aggiornamento dati anagrafici
        if (payload.name() != null
                && !Objects.equals(payload.name(), user.getName())) {
            user.setName(payload.name());
            personalDataChanged = true;
        }

        if (payload.surname() != null
                && !Objects.equals(payload.surname(), user.getSurname())) {
            user.setSurname(payload.surname());
            personalDataChanged = true;
        }

        if (payload.dateOfBirth() != null
                && !Objects.equals(payload.dateOfBirth(), user.getDateOfBirth())) {
            user.setDateOfBirth(payload.dateOfBirth());
            personalDataChanged = true;
        }

        if (payload.placeOfBirth() != null
                && !Objects.equals(payload.placeOfBirth(), user.getPlaceOfBirth())) {
            user.setPlaceOfBirth(payload.placeOfBirth());
            personalDataChanged = true;
        }

        if (payload.taxCode() != null
                && !Objects.equals(payload.taxCode(), user.getTaxCode())) {
            user.setTaxCode(payload.taxCode());
            personalDataChanged = true;
        }

        // 4. Aggiornamento dati di contatto e indirizzo
        if (payload.phoneNumber() != null) {
            user.setPhoneNumber(payload.phoneNumber());
        }

        if (payload.streetAddress() != null) {
            user.setStreetAddress(payload.streetAddress());
        }

        if (payload.houseNumber() != null) {
            user.setHouseNumber(payload.houseNumber());
        }

        if (payload.zipCode() != null) {
            user.setZipCode(payload.zipCode());
        }

        if (payload.city() != null) {
            user.setCity(payload.city());
        }

        if (payload.province() != null) {
            user.setProvince(payload.province());
        }

        // 5. Aggiornamento IBAN
        if (payload.iban() != null
                && !Objects.equals(payload.iban(), user.getIban())) {

            user.setIban(payload.iban());
            ibanChanged = true;
        }

        // 6. Aggiornamento dati documento
        if (payload.documentNumber() != null
                && !Objects.equals(
                payload.documentNumber(),
                user.getDocumentNumber()
        )) {

            user.setDocumentNumber(payload.documentNumber());
            documentDataChanged = true;
        }

        if (payload.documentType() != null
                && !Objects.equals(
                payload.documentType(),
                user.getDocumentType()
        )) {

            user.setDocumentType(payload.documentType());
            documentDataChanged = true;
        }

        if (payload.issueDate() != null
                && !Objects.equals(
                payload.issueDate(),
                user.getIssueDate()
        )) {

            user.setIssueDate(payload.issueDate());
            documentDataChanged = true;
        }

        if (payload.expirationDate() != null
                && !Objects.equals(
                payload.expirationDate(),
                user.getExpirationDate()
        )) {

            user.setExpirationDate(payload.expirationDate());
            documentDataChanged = true;
        }

        // 7. Se cambiano i dati del documento,
        //    sono obbligatorie entrambe le nuove foto
        if (documentDataChanged) {

            boolean hasFront = payload.documentFront() != null
                    && !payload.documentFront().isEmpty();

            boolean hasBack = payload.documentBack() != null
                    && !payload.documentBack().isEmpty();

            if (!hasFront || !hasBack) {
                throw new BadRequestException(
                        "Se modifichi i dati del documento, devi allegare "
                                + "anche le nuove foto di Fronte e Retro."
                );
            }
        }

        // 8. Caricamento opzionale dei file su Cloudinary
        String newAvatarUrl = uploadOptionalDocument(
                payload.photoUrl(),
                "Foto profilo"
        );

        String newFrontUrl = uploadOptionalDocument(
                payload.documentFront(),
                "Fronte documento"
        );

        String newBackUrl = uploadOptionalDocument(
                payload.documentBack(),
                "Retro Documento"
        );

        String newTaxFrontUrl = uploadOptionalDocument(
                payload.taxCodeCardFront(),
                "Fronte Codice Fiscale"
        );

        String newTaxBackUrl = uploadOptionalDocument(
                payload.taxCodeCardBack(),
                "Retro Codice Fiscale"
        );

        // 9. Assegnazione degli URL restituiti da Cloudinary
        boolean documentFilesChanged = false;

        if (newAvatarUrl != null
                && !Objects.equals(newAvatarUrl, user.getPhotoUrl())) {

            user.setPhotoUrl(newAvatarUrl);
        }

        if (newFrontUrl != null
                && !Objects.equals(newFrontUrl, user.getDocumentFrontUrl())) {

            user.setDocumentFrontUrl(newFrontUrl);
            documentFilesChanged = true;
        }

        if (newBackUrl != null
                && !Objects.equals(newBackUrl, user.getDocumentBackUrl())) {

            user.setDocumentBackUrl(newBackUrl);
            documentFilesChanged = true;
        }

        if (newTaxFrontUrl != null
                && !Objects.equals(
                newTaxFrontUrl,
                user.getTaxCodeCardFrontUrl()
        )) {

            user.setTaxCodeCardFrontUrl(newTaxFrontUrl);
            documentFilesChanged = true;
        }

        if (newTaxBackUrl != null
                && !Objects.equals(
                newTaxBackUrl,
                user.getTaxCodeCardBackUrl()
        )) {

            user.setTaxCodeCardBackUrl(newTaxBackUrl);
            documentFilesChanged = true;
        }

        // 10. Verifico se sono cambiati i documenti
        boolean documentChanged =
                documentDataChanged || documentFilesChanged;

        // 11. Salvo l'utente aggiornato
        User updatedUser = userRepository.save(user);

        // 12. Invio email se sono cambiati dati sensibili
        boolean sensitiveDataChanged =
                personalDataChanged
                        || ibanChanged
                        || documentChanged;

        if (sensitiveDataChanged) {

            List<String> recipientEmails =
                    accountService.getEmailsByRoles(
                            List.of(
                                    "ADMIN",
                                    "HR",
                                    "AP E PAYROLL SPECIALIST"
                            )
                    );

            if (!recipientEmails.isEmpty()) {

                String employeeName =
                        updatedUser.getName()
                                + " "
                                + updatedUser.getSurname();

                String details;

                if (personalDataChanged
                        && ibanChanged
                        && documentChanged) {

                    details = "i dati personali, l'IBAN e i documenti";

                } else if (personalDataChanged
                        && ibanChanged) {

                    details = "i dati personali e l'IBAN";

                } else if (personalDataChanged
                        && documentChanged) {

                    details = "i dati personali e i documenti";

                } else if (ibanChanged
                        && documentChanged) {

                    details = "l'IBAN e i documenti";

                } else if (personalDataChanged) {

                    details = "i dati personali";

                } else if (ibanChanged) {

                    details = "l'IBAN";

                } else {

                    details = "i documenti";
                }

                String htmlBody =
                        EmailTemplateBuilder.buildUpdateSensitiveData(
                                employeeName,
                                details,
                                ProfileUpdated
                        );

                String subject =
                        "[YouRoster] Modifica Dati Sensibili: "
                                + employeeName;

                for (String email : recipientEmails) {

                    try {
                        emailService.sendHtmlEmail(
                                email,
                                subject,
                                htmlBody
                        );

                    } catch (Exception e) {

                        System.err.println(
                                "Errore invio mail a "
                                        + email
                                        + ": "
                                        + e.getMessage()
                        );
                    }
                }
            }
        }

        // 13. Restituisco il profilo aggiornato
        return mapToDTO(updatedUser, currentAccount);
    }

    //3. GET TUTTI GLI UTENTI E LE SUE INFORMAZIONI(ADMIN, HR, PAYROLL)
    @Transactional
    public UserProfileResponseDTO getUserProfileForManagement(UUID userId) {
        User user = userRepository.findByIdWithOffice(userId).orElseThrow(() -> new NotFoundException("L'utente con id " + userId + " non è stato trovato."));
        Account account = user.getAccount();
        if (account == null) {
            throw new NotFoundException("Nessun account associato all'utente con id " + userId);
        }
        return mapToDTO(user, account);
    }

    //DATA UNA SEDE TROVA I COORDINATOR
    public Page<UserBasicInformationResponseDTO> findCoordinatorsByOfficeId(UUID officeId, Pageable pageable) {

        Office office = officeService.findById(officeId);
        Page<User> users = userRepository.findByReferenceOfficeAndAccountRolesName(office, "COORDINATOR", pageable);

        return users.map(user -> new UserBasicInformationResponseDTO(user.getId(), user.getName(), user.getSurname(), user.getPhoneNumber(), user.getAccount().getEmail(), user.getReferenceOffice().getName(), user.getAccount().getRoles().stream().map(Role::getName).collect(Collectors.toSet())));
    }


    // AGGIORNA SEDE DELL'UTENTE
    public UserBasicInformationResponseDTO updateUserOffice(UUID userId, UUID newOfficeId) {

        // Recupero l'utente
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utente non trovato"));

        // Recupero la nuova sede
        Office newOffice = officeService.findById(newOfficeId);

        // Assegno/sostituisco la sede
        user.setReferenceOffice(newOffice);

        User updatedUser = userRepository.save(user);

        // Ritorno le informazioni aggiornate
        return new UserBasicInformationResponseDTO(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getSurname(),
                updatedUser.getPhoneNumber(),
                updatedUser.getAccount().getEmail(),
                updatedUser.getReferenceOffice().getName(),
                updatedUser.getAccount().getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    //UDATE AVATAR
    @Transactional
    public UserProfileResponseDTO updateAvatar(Account currentAccount, MultipartFile avatarFile) {
        if (currentAccount.getUser() == null) {
            throw new NotFoundException("Nessun profilo utente è associato a questo account");
        }

        User user = userRepository.findById(currentAccount.getUser().getId()).orElseThrow(() -> new NotFoundException("User non trovato"));

        // Carica su Cloudinary
        String newAvatarUrl = uploadOptionalDocument(avatarFile, "Foto profilo");

        if (newAvatarUrl != null) {
            user.setPhotoUrl(newAvatarUrl);
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser, currentAccount);
    }

    //GET USER PER ASSEGNARE TURNO
    public List<UserForShiftAssignmentDTO> getAllActiveUsersForAssignment() {
        List<User> activeUsers = userRepository.findByAccount_Status(AccountStatus.ACTIVE);

        return activeUsers.stream()
                .map(user -> new UserForShiftAssignmentDTO(
                        user.getId(),
                        user.getName() + " " + user.getSurname()
                ))
                .toList();
    }

    //GET TUTTI GLI UTENTI PER ADMIN E HR
    @Transactional
    public Page<UserProfileResponseDTO> getAllUsersForManagement(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> {

            Account account = user.getAccount();

            if (account == null) {
                throw new NotFoundException(
                        "Nessun account associato all'utente con id " + user.getId()
                );
            }

            return mapToDTO(user, account);
        });
    }

}


