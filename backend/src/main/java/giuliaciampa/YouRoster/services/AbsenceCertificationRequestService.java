package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.AbsenceCertificationRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateCertificationRequestDTO;
import giuliaciampa.YouRoster.dto.responses.AbsenceCertificationResponseDTO;
import giuliaciampa.YouRoster.dto.responses.AbsenceCertificationReviewResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.AbsenceCertificationRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static giuliaciampa.YouRoster.entities.AssignmentType.SICK;

@Service
public class AbsenceCertificationRequestService {

    private final AbsenceCertificationRequestRepository absenceCertificationRequestRepository;
    private final UserService userService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftService shiftService;
    private final AccountService accountService;
    private final EmailService emailService;
    //LINK
    @Value("${newCertificationRequest.url}")
    private String newCertificationRequest;

    public AbsenceCertificationRequestService(AbsenceCertificationRequestRepository absenceCertificationRequestRepository, UserService userService, ShiftAssignmentService shiftAssignmentService, ShiftService shiftService, AccountService accountService, EmailService emailService) {
        this.absenceCertificationRequestRepository = absenceCertificationRequestRepository;
        this.userService = userService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.shiftService = shiftService;
        this.accountService = accountService;
        this.emailService = emailService;
    }

    //METODO TRADUZIONE ENUM PER INVIO EMAIL
    private static String getCertificationLabel(CertificateType type) {
        return switch (type) {
            case SICKNESS -> "malattia";
            case PATERNITY -> "paternità";
            case MATERNITY -> "maternità";
            case PARENTAL_LEAVE -> "congedo parentale";
            default -> "motivo generico";
        };
    }

    //HELPER PER LA RISPOSTA
    private AbsenceCertificationResponseDTO mapToResponse(AbsenceCertificationRequest request) {
        return new AbsenceCertificationResponseDTO(
                request.getId(),
                request.getProtocolCode(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalDays(),
                request.getIssueDate(),
                request.getCertificateUrl(),
                request.getCertificateType(),
                request.getRequestStatus(),
                request.getCreatedAt(),
                request.getEmployeeNotes(),
                request.getReviewerNotes(),
                request.getEmployee().getName() + " " + request.getEmployee().getSurname()
        );
    }

    //METODO ASSIGNMENT TYPE
    private AssignmentType findAssignmentTypeForRequest(User employee, LocalDate date) {
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(employee, date);
        // O un valore di default se non c'è turno
        return optionalShift.map(ShiftAssignment::getAssignmentType).orElse(null);
    }

    //HELPER RISPOSTA ADMIN O HR
    private AbsenceCertificationReviewResponseDTO mapToReviewerResponse(AbsenceCertificationRequest request) {


        // 1. Gestione le note del reviewer: se non è ancora stata revisionata o il reviewer è null, mettiamo il messaggio di default
        String reviewerNotesResponse;
        if (request.getRequestStatus() == RequestStatus.SENT) {
            reviewerNotesResponse = "La richiesta non è ancora stata revisionata";
        } else {
            reviewerNotesResponse = request.getReviewerNotes() != null ? request.getReviewerNotes() : "Nessuna nota inserita";
        }


        //reviewer
        String reviewer = null;

        if (request.getReviewer() != null) {

            Account reviewerAccount = request.getReviewer().getAccount();

            boolean isAdmin = reviewerAccount.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));

            if (isAdmin) {
                reviewer = "ADMIN";
            } else {
                reviewer = request.getReviewer().getName()
                        + " "
                        + request.getReviewer().getSurname();
            }
        }

        //ruoli
        List<String> reviewerRoles =
                (request.getReviewer() != null
                        && request.getReviewer().getAccount() != null
                        && request.getReviewer().getAccount().getRoles() != null)
                        ? request.getReviewer().getAccount().getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList()
                        : List.of();

        // 3. Recuperiamo l'AssignmentType per la data di inizio (o gestiscilo come preferisci)
        AssignmentType currentAssignmentType = null;
        Optional<ShiftAssignment> assignmentOpt = shiftAssignmentService.findByUserAndShiftDateOptional(request.getEmployee(), request.getStartDate());
        if (assignmentOpt.isPresent()) {
            currentAssignmentType = assignmentOpt.get().getAssignmentType();
        }

        return new AbsenceCertificationReviewResponseDTO(
                request.getId(),
                request.getProtocolCode(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTotalDays(),
                request.getIssueDate(),
                request.getCertificateUrl(),
                request.getCertificateType(),
                request.getRequestStatus(),
                request.getCreatedAt(),
                request.getEmployeeNotes(),
                reviewerNotesResponse,
                reviewerRoles,
                reviewer,
                currentAssignmentType,
                request.getEmployee().getName() + " " + request.getEmployee().getSurname()
        );


    }

    //HELPER ACCOUNT AUTENTICATO E' ADMIN O HR
    // Metodo di supporto privato per verificare che l'account sia un Admin
    private void validateAdminAndHrRole(Account currentAccount) {

        if (currentAccount == null || currentAccount.getRoles() == null ||
                currentAccount.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()) || "HR".equals(role.getName()))) {


            throw new UnauthorizedException("Accesso negato: solo l'amministratore o il responsabile risorse umane possono visualizzare queste richieste.");
        }
    }

    //HELPER PER CAMBIARE LO STATO DELL'ASSEGNAZIONE
    private AssignmentType mapCertificateToAssignmentType(CertificateType certificateType) {
        return switch (certificateType) {
            case SICKNESS -> SICK;
            case MATERNITY -> AssignmentType.MATERNITY;
            case PATERNITY -> AssignmentType.PATERNITY;
            case PARENTAL_LEAVE -> AssignmentType.PARENTAL_LEAVE;
            default -> AssignmentType.ABSENT;
        };
    }


    //1. NUOVA RICHIESTA CERTIFICATA
    public AbsenceCertificationResponseDTO newCertificationRequest(Account currentAccount, AbsenceCertificationRequestDTO payload) {

        //1. controllo numero protocollo
        if (absenceCertificationRequestRepository.existsByProtocolCode(payload.protocolCode())) {
            throw new BadRequestException("Il numero di protocollo inserito " + payload.protocolCode() + " esiste già. Digita un nuovo numero di protocollo.");
        }


        //2. controllo date
        if (payload.startDate().isAfter(payload.endDate())) {
            throw new BadRequestException("La data di inizio non può essere successiva alla data di fine.");
        }

        //3. upload certificato
        String certificateUrl = userService.uploadDocuments(payload.certificateFile(), "Certificato Inps");


        //4. popolamento entità
        AbsenceCertificationRequest newRequest = new AbsenceCertificationRequest();
        newRequest.setProtocolCode(payload.protocolCode());
        newRequest.setStartDate(payload.startDate());
        newRequest.setEndDate(payload.endDate());
        newRequest.setIssueDate(payload.issueDate());
        newRequest.setCertificateUrl(certificateUrl);
        newRequest.setCertificateType(payload.certificateType());
        newRequest.setEmployeeNotes(payload.employeeNotes());
        newRequest.setRequestStatus(RequestStatus.SENT);
        newRequest.setCreatedAt(LocalDateTime.now());

        //5. controllo sull'account autenticato
        if (currentAccount == null || currentAccount.getUser() == null) {
            throw new UnauthorizedException("Utente non autenticato correttamente.");
        }
        newRequest.setEmployee(currentAccount.getUser());

        //6. chiamo il metodo calcolo dei giorni
        newRequest.calculateTotalDays();

        //7. salvo
        AbsenceCertificationRequest savedRequest = absenceCertificationRequestRepository.save(newRequest);

        //-------invio email
        List<String> rolesToNotify = List.of("HR", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String translatedType = getCertificationLabel(newRequest.getCertificateType());

            String htmlBody = EmailTemplateBuilder.buildNewCertificationRequest(recipientRole, newRequest.getEmployee().getName(), newRequest.getEmployee().getSurname(), translatedType, newRequest.getStartDate(), newRequest.getEndDate(), newCertificationRequest);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova richiesta" + translatedType + " - YouRoster!]",
                    htmlBody);

        }


        //8. risposta
        return mapToResponse(savedRequest);

    }

    //2. MODIFICA RICHIESTA
    public AbsenceCertificationResponseDTO updateRequest(Account currentAccount, UpdateCertificationRequestDTO payload, UUID requestId) {
        //1 trova la richiesta
        AbsenceCertificationRequest request = absenceCertificationRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        //2 controlla stato se sent passa
        if (request.getRequestStatus() != RequestStatus.SENT &&
                request.getRequestStatus() != RequestStatus.APPROVED) {

            throw new BadRequestException(
                    "Non puoi modificare questa richiesta."
            );
        }

        //controllo date, se è già passata
        if (request.getStartDate().isBefore(LocalDate.now()) || request.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Non puoi modificare una richiesta relativa ad una data passata!");
        }

        // Aggiorna solo se il payload contiene un nuovo valore
        if (payload.protocolCode() != null && !payload.protocolCode().isBlank()) {
            request.setProtocolCode(payload.protocolCode());
        }

        if (payload.startDate() != null) {
            request.setStartDate(payload.startDate());
        }

        if (payload.endDate() != null) {
            request.setEndDate(payload.endDate());
        }

        if (payload.issueDate() != null) {
            request.setIssueDate(payload.issueDate());
        }

        if (payload.certificateFile() != null && !payload.certificateFile().isEmpty()) {
            String uploadedUrl = userService.uploadDocuments(
                    payload.certificateFile(),
                    "Certificato Inps"
            );
            request.setCertificateUrl(uploadedUrl);
        }

        if (payload.certificateType() != null) {
            request.setCertificateType(payload.certificateType());
        }

        if (payload.employeeNotes() != null) {
            request.setEmployeeNotes(payload.employeeNotes());
        }

        request.calculateTotalDays();


        //3 aggiorna solo campi che vuoi aggiornare

        AbsenceCertificationRequest updatedRequest = absenceCertificationRequestRepository.save(request);

        //4. return
        return mapToResponse(updatedRequest);
    }


    //3. ELIMINA RICHIESTA SE LO STATO È ANCORA SENTI

    public void deleteRequestIfSentStatus(Account currentAccount, UUID requestId) {
        //1. trova richiesta
        AbsenceCertificationRequest request = absenceCertificationRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata."));

        //2. controlla status
        if (request.getRequestStatus() == RequestStatus.SENT) {

            //controllo date, se è già passata
            if (request.getStartDate().isBefore(LocalDate.now()) || request.getEndDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Non puoi eliminare una richiesta relativa ad una data passata!");
            }


            absenceCertificationRequestRepository.delete(request);
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }
    }

    //4. VISUALIZZA LE TUE RICHIESTE CERTIFICATE
    public Page<AbsenceCertificationReviewResponseDTO> getMyCertifications(Account currentAccount, LocalDate startDate, LocalDate endDate, RequestStatus status, Pageable pageable) {

        Page<AbsenceCertificationRequest> requests;

        // Se l'utente ha inserito entrambe le date, filtro per range
        if (startDate != null && endDate != null) {
            requests = absenceCertificationRequestRepository.findByEmployeeAndStartDateBetween(
                    currentAccount.getUser(), startDate, endDate, pageable);
        } else if (startDate != null) {

            requests = absenceCertificationRequestRepository.findByEmployeeAndStartDate(currentAccount.getUser(), startDate, pageable);

        } else if (status != null) {
            requests = absenceCertificationRequestRepository.findByEmployeeAndRequestStatus(currentAccount.getUser(), status, pageable);
        } else {
            // Altrimenti prendiamo tutte le sue richieste
            requests = absenceCertificationRequestRepository.findByEmployee(currentAccount.getUser(), pageable);
        }

        //map
        return requests.map(this::mapToReviewerResponse);
    }


    //5. METODO GET RICHIESTE CERTIFICATE CON FILTRI OPZIONALI
    public Page<AbsenceCertificationReviewResponseDTO> getAllWithFilters(Account currentAccount, String name, RequestStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 1. Controllo di sicurezza
        validateAdminAndHrRole(currentAccount);

        // 2. Esecuzione query con la stringa di ricerca (nome o cognome)
        return absenceCertificationRequestRepository.findWithFilters(name, status, startDate, endDate, pageable)
                .map(this::mapToReviewerResponse);
    }


    //6. METODO GET RICHIESTE DA LAVORARE(STATO SENT)
    public Page<AbsenceCertificationResponseDTO> getPendingRequests(
            Account currentAccount,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        validateAdminAndHrRole(currentAccount);

        boolean isHr = currentAccount.getRoles().stream()
                .anyMatch(role -> "HR".equals(role.getName()));

        if (isHr) {
            return absenceCertificationRequestRepository
                    .findPendingRequestsForHr(
                            name,
                            RequestStatus.SENT,
                            currentAccount.getUser(),
                            startDate,
                            endDate,
                            pageable
                    )
                    .map(this::mapToResponse);
        }

        return absenceCertificationRequestRepository
                .findWithFilters(
                        name,
                        RequestStatus.SENT,
                        startDate,
                        endDate,
                        pageable
                )
                .map(this::mapToResponse);
    }

    //7. ADMIN O HR VALUTANO LA RICHIESTA(APPROVA)
    public AbsenceCertificationReviewResponseDTO approveRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        // 1. Controllo di sicurezza
        validateAdminAndHrRole(currentAccount);

        // 2. Trova la richiesta
        AbsenceCertificationRequest request = absenceCertificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata."));


        // 3. Verifica che la richiesta sia effettivamente in stato SENT (quindi ancora da valutare)
        if (request.getRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException("Questa richiesta è già stata valutata o non è in uno stato valido per la valutazione.");
        }


        //4. IMPOSTA REVISORE SOLO SE ESISTE UNO USER ASSOCIATO
        if (currentAccount.getUser() != null) {
            request.setReviewer(currentAccount.getUser());
        }

        request.setRequestStatus(RequestStatus.APPROVED);
        request.setResponseDate(LocalDate.now());

        String reviewerNotes = null;
        if (payload != null) {
            reviewerNotes = payload.notes();
        }

        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String translatedType = getCertificationLabel(request.getCertificateType());

            String htmlBody = EmailTemplateBuilder.buildApproveCertificationRequest(recipientRole, request.getEmployee().getName(), request.getEmployee().getSurname(), request.getStartDate(), request.getEndDate(), translatedType);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova approvazione richiesta" + translatedType + " - YouRoster!]",
                    htmlBody);
        }

        //------- CAMBIO TIPO / CREAZIONE SHIFT ASSIGNMENT PER INTERO RANGE

        LocalDate current = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        AssignmentType newType = mapCertificateToAssignmentType(request.getCertificateType());

        while (!current.isAfter(endDate)) {
            // Controlliamo se esiste già un'assegnazione per questa specifica data
            Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(
                    request.getEmployee(),
                    current
            );

            ShiftAssignment shiftToUpdate;
            if (optionalShift.isPresent()) {
                shiftToUpdate = optionalShift.get();
            } else {
                shiftToUpdate = new ShiftAssignment();
                shiftToUpdate.setUser(request.getEmployee());
                shiftToUpdate.setShiftDate(current);
                // Creiamo l'oggetto Shift che mancava
                Shift newShift = new Shift();
                Shift savedShift = shiftService.save(newShift);

                // Colleghiamo lo Shift al ShiftAssignment
                shiftToUpdate.setShift(savedShift);
            }

            shiftToUpdate.setAssignmentType(newType);
            shiftAssignmentService.save(shiftToUpdate);

            current = current.plusDays(1);
        }
        //-------------------------

        // 5. Salva e ritorna il DTO mappato
        AbsenceCertificationRequest updatedRequest = absenceCertificationRequestRepository.save(request);

        return mapToReviewerResponse(updatedRequest);
    }


    //8. ADMIN O HR VALUTANO LA RICHIESTA(RIFIUTA)
    public AbsenceCertificationReviewResponseDTO rejectRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        // 1. Controllo di sicurezza
        validateAdminAndHrRole(currentAccount);

        // 2. Trova la richiesta
        AbsenceCertificationRequest request = absenceCertificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata."));

        // 3. Verifica che la richiesta sia effettivamente in stato SENT (quindi ancora da valutare)
        if (request.getRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException("Questa richiesta è già stata valutata o non è in uno stato valido per la valutazione.");
        }


        //4. IMPOSTA REVISORE SOLO SE ESISTE UNO USER ASSOCIATO
        if (currentAccount.getUser() != null) {
            request.setReviewer(currentAccount.getUser());
        }

        request.setRequestStatus(RequestStatus.REJECTED);
        request.setResponseDate(LocalDate.now());

        String reviewerNotes = null;
        if (payload != null) {
            reviewerNotes = payload.notes();
        }
        request.setReviewerNotes(reviewerNotes);

        // 5. Salva e ritorna il DTO mappato
        AbsenceCertificationRequest updatedRequest = absenceCertificationRequestRepository.save(request);
        return mapToReviewerResponse(updatedRequest);
    }

    //GET RICHIESTE APPROVATE
    public Page<AbsenceCertificationReviewResponseDTO> getApprovedCertifications(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return absenceCertificationRequestRepository
                .findApprovedOverlappingRange(
                        RequestStatus.APPROVED,
                        startDate,
                        endDate,
                        pageable
                )
                .map(this::mapToReviewerResponse);
    }
}
