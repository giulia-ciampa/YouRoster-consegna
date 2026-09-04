package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ChangeLeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeLeaveHoursRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeLeaveHoursRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ChangeLeaveHoursRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChangeLeaveHoursRequestService {

    private final ChangeLeaveHoursRequestRepository changeLeaveHoursRequestRepository;
    private final AccountService accountService;
    private final EmailService emailService;
    private final LeaveHoursRequestService leaveHoursRequestService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftService shiftService;
    //LINK
    @Value("${newChangeLeaveHoursRequest.url}")
    private String newChangeRequestUrl;

    @Value("${response.url}")
    private String urlDashboard;

    public ChangeLeaveHoursRequestService(ChangeLeaveHoursRequestRepository changeLeaveHoursRequestRepository, AccountService accountService, EmailService emailService, LeaveHoursRequestService leaveHoursRequestService, ShiftAssignmentService shiftAssignmentService, ShiftService shiftService) {
        this.changeLeaveHoursRequestRepository = changeLeaveHoursRequestRepository;
        this.accountService = accountService;
        this.emailService = emailService;
        this.leaveHoursRequestService = leaveHoursRequestService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.shiftService = shiftService;
    }

    //HELPER DTO DI RISPOSTA
    public ChangeLeaveHoursRequestResponseDTO mapToResponse(ChangeLeaveHoursRequest request) {

        String reviewerName = null;

        if (request.getReviewer() != null) {

            boolean isAdmin = request.getReviewer()
                    .getAccount()
                    .getRoles()
                    .stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));

            if (isAdmin) {
                reviewerName = "ADMIN";
            } else {
                reviewerName = request.getReviewer().getName()
                        + " "
                        + request.getReviewer().getSurname();
            }
        }

        return new ChangeLeaveHoursRequestResponseDTO(
                request.getId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getEmployeeNotes(),
                request.getOriginalRequest().getEmployeeNotes(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getStartTime(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getEndTime(),
                request.getCreatedAt(),
                request.getOriginalRequest().getCreatedAt(),
                request.getChangeRequestStatus(),
                request.getReviewCount(),
                request.getTotalHours(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getTotalHours(),
                reviewerName,
                request.getResponseDate(),
                request.getChangeLeaveHoursType(),
                request.getOriginalRequest().getEmployee().getName() + " " + request.getOriginalRequest().getEmployee().getSurname(),
                request.getReviewerNotes(),
                request.getOriginalRequest().getReviewerNotes(),
                request.getOriginalRequest().getResponseDate(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getDate());
    }

    //METODO ASSIGNMENT TYPE
    private AssignmentType findAssignmentTypeForRequest(User employee, LocalDate date) {
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(employee, date);
        // O un valore di default se non c'è turno
        return optionalShift.map(ShiftAssignment::getAssignmentType).orElse(null);
    }

    //HELPER REVIEW RESPONSE
    public ChangeLeaveHoursRequestReviewerResponseDTO mapToReviewerResponse(ChangeLeaveHoursRequest request) {


        // 1. Gestione le note del reviewer: se non è ancora stata revisionata o il reviewer è null, mettiamo il messaggio di default
        String reviewerNotesResponse;
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {
            reviewerNotesResponse = "La richiesta non è ancora stata revisionata";
        } else {
            reviewerNotesResponse = request.getReviewerNotes() != null ? request.getReviewerNotes() : "Nessuna nota inserita";
        }


        String reviewerName = null;

        if (request.getReviewer() != null) {

            boolean isAdmin = request.getReviewer()
                    .getAccount()
                    .getRoles()
                    .stream()
                    .anyMatch(role -> role.getName().equals("ADMIN"));

            if (isAdmin) {
                reviewerName = "ADMIN";
            } else {
                reviewerName = request.getReviewer().getName()
                        + " "
                        + request.getReviewer().getSurname();
            }
        }

        AssignmentType currentAssignmentType = findAssignmentTypeForRequest(request.getOriginalRequest().getEmployee(), request.getDate());


        return new ChangeLeaveHoursRequestReviewerResponseDTO(
                request.getId(),
                request.getOriginalRequest().getId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getEmployeeNotes(),
                request.getOriginalRequest().getEmployeeNotes(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getStartTime(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getEndTime(),
                request.getCreatedAt(),
                request.getOriginalRequest().getCreatedAt(),
                request.getChangeRequestStatus(),
                request.getReviewCount(),
                request.getResponseDate(),
                reviewerNotesResponse,
                reviewerName,
                currentAssignmentType,
                request.getTotalHours(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getTotalHours(),
                request.getOriginalRequest().getReviewerNotes(),
                request.getOriginalRequest().getResponseDate(),
                ((LeaveHoursRequest) request.getOriginalRequest()).getDate()
        );
    }

    //HELPER ACCOUNT AUTENTICATO E' ADMIN O SHIFT MANAGER
    private void validateAdminAndShiftManagerRole(Account currentAccount) {

        if (currentAccount == null || currentAccount.getRoles() == null ||
                currentAccount.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()) || "SHIFT MANAGER".equals(role.getName()))) {


            throw new UnauthorizedException("Accesso negato: solo l'amministratore o il responsabile turni possono visualizzare queste richieste.");
        }
    }

    //1. NUOVA RICHIESTA DI MODIFICA
    public ChangeLeaveHoursRequestResponseDTO SaveChangeRequest(Account currentAccount, UUID originalRequestId, ChangeLeaveHoursRequestDTO payload) {
        User userAutenticato = currentAccount.getUser();

        //1. recupera richiesta precedente
        LeaveHoursRequest originalRequest = leaveHoursRequestService.findById(originalRequestId);


        //2. vedi lo stato
        if (originalRequest.getRequestStatus() == RequestStatus.SENT) {
            throw new BadRequestException("Non puoi richiedere una modifica per una richiesta ancora in attesa di approvazione.");
        }

        //controllo se esiste già una richiesta di modifica per quella richiesta
        boolean exists =
                changeLeaveHoursRequestRepository
                        .existsByOriginalRequest_IdAndChangeRequestStatus(
                                originalRequestId,
                                RequestStatus.SENT
                        );
        if (exists) {
            throw new BadRequestException("Hai già inviato una richiesta di modifica. Presto verrà valutata.");
        }

        //3. compila campi nuova richiesta
        ChangeLeaveHoursRequest changeRequest = new ChangeLeaveHoursRequest(
                originalRequest,
                payload.employeeNotes(),
                payload.date(),
                payload.startTime(),
                payload.endTime(),
                payload.leaveHoursType()
        );


        //4. salva
        ChangeLeaveHoursRequest savedChangeRequest = changeLeaveHoursRequestRepository.save(changeRequest);

        //5. invio email

        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.buildChangeLeaveHoursRequest(recipientRole, currentAccount.getUser().getName(), currentAccount.getUser().getSurname(), changeRequest.getDate(), changeRequest.getStartTime(), changeRequest.getEndTime(), newChangeRequestUrl);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova richiesta di modifica - YouRoster!]",
                    htmlBody);
        }
        //6. return
        return mapToResponse(savedChangeRequest);
    }

    //2. MODIFICA RICHIESTA DI MODIFICA IF STATUS SENT
    public ChangeLeaveHoursRequestResponseDTO updateRequestIfSent(Account currentAccount, ChangeLeaveHoursRequestDTO payload, UUID requestId) {
        ChangeLeaveHoursRequest request = changeLeaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));
        //2 controlla status se sent passa
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {

            boolean timeChanged = false;

            if (payload.startTime() != null) {
                request.setStartTime(payload.startTime());
                timeChanged = true;
            }

            if (payload.endTime() != null) {
                request.setEndTime(payload.endTime());
                timeChanged = true;
            }

            // Ricalcoliamo i giorni totali SOLO SE è stata modificata almeno una delle date
            if (timeChanged) {
                if (request.getStartTime().isAfter(request.getEndTime())) {
                    throw new BadRequestException("L'orario di inizio non può essere successivo all'orario di fine.");
                }
                request.calculateTotalHours();
            }

            if (payload.date() != null) {
                request.setDate(payload.date());
            }

            if (payload.employeeNotes() != null) {
                request.setEmployeeNotes(payload.employeeNotes());
            }
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }

        //3 salva
        ChangeLeaveHoursRequest savedRequest = changeLeaveHoursRequestRepository.save(request);

        //return
        return mapToResponse(savedRequest);

    }


    //3. ELIMINA RICHIESTA DI MODIFICA IF STATUS SENT
    public void DeleteIfStatusIsSent(Account currentAccount, UUID requestId) {
        ChangeLeaveHoursRequest request = changeLeaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        //2. controlla che lo stato è sent, se si, elimina, altrimenti messaggio di errore
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {
            changeLeaveHoursRequestRepository.delete(request);
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }

    }

    //4. GET MY REQUESTS
    public Page<ChangeLeaveHoursRequestReviewerResponseDTO> getMyRequests(Account currentAccount, LocalDate date, LocalTime startTime, LocalTime endTime, RequestStatus status, Pageable pageable) {
        Page<ChangeLeaveHoursRequest> requests;

        // Se l'utente ha inserito entrambi gli orari, filtro per range
        if (startTime != null && endTime != null) {
            requests = changeLeaveHoursRequestRepository.findByOriginalRequest_EmployeeAndStartTimeBetween(currentAccount.getUser(), startTime, endTime, pageable);
        } else if (startTime != null) {
            requests = changeLeaveHoursRequestRepository.findByOriginalRequest_EmployeeAndStartTime(currentAccount.getUser(), startTime, pageable);
        } else if (status != null) {
            requests = changeLeaveHoursRequestRepository.findByOriginalRequest_EmployeeAndChangeRequestStatus(currentAccount.getUser(), status, pageable);
        } else {
            // Altrimenti prendiamo tutte le sue richieste
            requests = changeLeaveHoursRequestRepository.findByOriginalRequest_Employee(currentAccount.getUser(), pageable);
        }

        //map
        return requests.map(this::mapToReviewerResponse);
    }

    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    public Page<ChangeLeaveHoursRequestReviewerResponseDTO> getAllWithFilters(Account currentAccount, String name, RequestStatus status, LocalDate date, LocalTime startTime, LocalTime endTime, Pageable pageable) {
        // 1. Controllo di sicurezza
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Esecuzione query con la stringa di ricerca (nome o cognome)
        return changeLeaveHoursRequestRepository.findWithFilters(name, status, date, startTime, endTime, pageable).map(this::mapToReviewerResponse);
    }


    //6. GET REQUEST DA LAVORARE (SENT)
    public Page<ChangeLeaveHoursRequestResponseDTO> getPendingRequests(Account currentAccount, String name, LocalDate date, LocalTime startTime, LocalTime endTime, Pageable pageable) {

        // 1. Controllo di sicurezza
        validateAdminAndShiftManagerRole(currentAccount);

        return changeLeaveHoursRequestRepository.findWithFilters(name, RequestStatus.SENT, date, startTime, endTime, pageable).map(this::mapToResponse);
    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED"
    public ChangeLeaveHoursRequestReviewerResponseDTO cancelRequest(Account currentAccount, UUID requestId) {

        //1. trova richiesta
        ChangeLeaveHoursRequest request = changeLeaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        //2 Verifica che la richiesta appartenga all'utente autenticato
        if (!request.getOriginalRequest()
                .getEmployee()
                .equals(currentAccount.getUser())) {

            throw new BadRequestException(
                    "Non puoi annullare una richiesta di modifica che non ti appartiene."
            );
        }

        // 2. Verifica che la richiesta sia effettivamente in stato APPROVED
        if (request.getChangeRequestStatus() != RequestStatus.APPROVED) {
            throw new BadRequestException("Puoi annullare solo una richiesta che era stata precedentemente approvata.");
        }

        //3 Puoi annullarla solo se il responsabile non l'ha ancora valutata
        if (request.getChangeRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException(
                    "Puoi annullare la richiesta di modifica solo se è ancora in attesa di valutazione."
            );
        }

        // 4 Cambia lo stato in CANCELLED
        request.setChangeRequestStatus(RequestStatus.CANCELLED);

        //5 salva
        ChangeLeaveHoursRequest savedRequest = changeLeaveHoursRequestRepository.save(request);

        //invio email
        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.builCancelledLeaveHoursRequest(recipientRole, currentAccount.getUser().getName(), currentAccount.getUser().getSurname(), request.getDate(), request.getStartTime(), request.getEndTime());

            emailService.sendHtmlEmail(recipientEmail,
                    "[Richiesta annullata - YouRoster!]",
                    htmlBody);
        }

        return mapToReviewerResponse(savedRequest);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    public ChangeLeaveHoursRequestReviewerResponseDTO approveRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {

        validateAdminAndShiftManagerRole(currentAccount);

        ChangeLeaveHoursRequest request = changeLeaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        // 3. Verifica che la richiesta sia effettivamente in stato SENT (quindi ancora da valutare)
        if (request.getChangeRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException("Questa richiesta è già stata valutata o non è in uno stato valido per la valutazione.");
        }

        //4. IMPOSTA REVISORE SOLO SE ESISTE UNO USER ASSOCIATO
        if (currentAccount.getUser() != null) {
            request.setReviewer(currentAccount.getUser());
        }

        request.setChangeRequestStatus(RequestStatus.APPROVED);
        request.setResponseDate(LocalDate.now());

        String reviewerNotes = null;

        if (payload != null) {
            reviewerNotes = payload.notes();
        }

        request.setReviewerNotes(reviewerNotes);

        // ------- CAMBIO TIPO SHIFT ASSIGNMENT
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(
                request.getOriginalRequest().getEmployee(),
                request.getDate()
        );

        ShiftAssignment shiftToUpdate;

        if (optionalShift.isPresent()) {
            shiftToUpdate = optionalShift.get();
        } else {
            // Caso B: Il turno NON esiste, creiamo tutto la catena
            shiftToUpdate = new ShiftAssignment();
            shiftToUpdate.setUser(request.getOriginalRequest().getEmployee());
            shiftToUpdate.setShiftDate(request.getDate());

            // Creiamo l'oggetto Shift che mancava
            Shift newShift = new Shift();
            newShift.setStartTime(request.getStartTime());
            newShift.setEndTime(request.getEndTime());
            Shift savedShift = shiftService.save(newShift);

            // Colleghiamo lo Shift al ShiftAssignment
            shiftToUpdate.setShift(savedShift);
        }

        // Ora impostiamo l'AssignmentType e salviamo
        shiftToUpdate.setAssignmentType(AssignmentType.PERMISSION);
        shiftAssignmentService.save(shiftToUpdate);

        //3. salva
        ChangeLeaveHoursRequest updatedRequest = changeLeaveHoursRequestRepository.save(request);

        //invio email

        String htmlBody = EmailTemplateBuilder.buildResponse(request.getOriginalRequest().getEmployee().getName(), urlDashboard);

        emailService.sendHtmlEmail(request.getOriginalRequest().getEmployee().getAccount().getEmail(),
                "[Richiesta approvata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);
    }


    //9. ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    public ChangeLeaveHoursRequestReviewerResponseDTO rejectRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        //1. validazione
        validateAdminAndShiftManagerRole(currentAccount);

        //2. trova la richiesta
        ChangeLeaveHoursRequest request = changeLeaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        // 3. Verifica che la richiesta sia effettivamente in stato SENT (quindi ancora da valutare)
        if (request.getChangeRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException("Questa richiesta è già stata valutata o non è in uno stato valido per la valutazione.");
        }

        //4. IMPOSTA REVISORE SOLO SE ESISTE UNO USER ASSOCIATO
        if (currentAccount.getUser() != null) {
            request.setReviewer(currentAccount.getUser());
        }

        request.setChangeRequestStatus(RequestStatus.REJECTED);
        request.setResponseDate(LocalDate.now());

        String reviewerNotes = null;

        if (payload != null) {
            reviewerNotes = payload.notes();
        }

        request.setReviewerNotes(reviewerNotes);

        // 5. Salva
        ChangeLeaveHoursRequest updatedRequest = changeLeaveHoursRequestRepository.save(request);

        //invio email
        String htmlBody = EmailTemplateBuilder.buildResponse(request.getOriginalRequest().getEmployee().getName(), urlDashboard);

        emailService.sendHtmlEmail(request.getOriginalRequest().getEmployee().getAccount().getEmail(),
                "[Richiesta rifiutata - YouRoster!]",
                htmlBody);
        return mapToReviewerResponse(updatedRequest);
    }

    // GET SINGOLA RICHIESTA DI MODIFICA PERMESSO ORE
    public ChangeLeaveHoursRequestResponseDTO getMyChangeRequest(
            Account currentAccount,
            UUID originalRequestId
    ) {

        ChangeLeaveHoursRequest request =
                changeLeaveHoursRequestRepository
                        .findByOriginalRequest_IdAndOriginalRequest_Employee(
                                originalRequestId,
                                currentAccount.getUser()
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Nessuna richiesta di modifica trovata"
                                )
                        );

        return mapToResponse(request);
    }

    //GET SINGOLA RICHIESTA PER SHIFT MANAGER E ADMIN
    public ChangeLeaveHoursRequestResponseDTO getChangeRequestForReviewer(UUID requestId) {


        ChangeLeaveHoursRequest request =
                changeLeaveHoursRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Nessuna richiesta di modifica trovata"
                                )
                        );

        return mapToResponse(request);
    }

}
