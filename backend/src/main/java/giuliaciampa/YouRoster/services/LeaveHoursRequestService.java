package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.LeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateLeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.responses.LeaveHoursRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.LeaveHoursRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.LeaveHoursRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LeaveHoursRequestService {
    private final LeaveHoursRequestRepository leaveHoursRequestRepository;
    private final ContractService contractService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final EmailService emailService;
    private final AccountService accountService;
    private final ShiftService shiftService;
    //LINK
    @Value("${newLeaveHoursRequest.url}")
    private String newRequestUrl;

    @Value("${response.url}")
    private String urlDashboard;

    public LeaveHoursRequestService(LeaveHoursRequestRepository leaveHoursRequestRepository, ContractService contractService, ShiftAssignmentService shiftAssignmentService, EmailService emailService, AccountService accountService, ShiftService shiftService) {
        this.leaveHoursRequestRepository = leaveHoursRequestRepository;
        this.contractService = contractService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.emailService = emailService;
        this.accountService = accountService;
        this.shiftService = shiftService;
    }

    //METODO PER PAYROLLSERVICE E SUMMARY SERVICE
    public List<LeaveHoursRequest> findByEmployeeAndRequestStatusAndDateBetween(User employee, RequestStatus status, LocalDate startDate, LocalDate endDate) {
        return leaveHoursRequestRepository.findByEmployeeAndRequestStatusAndDateBetween(employee, status, startDate, endDate);
    }

    //METODO HELPER DI RISPOSTA
    public LeaveHoursRequestResponseDTO mapToResponse(LeaveHoursRequest request) {
        return new LeaveHoursRequestResponseDTO(
                request.getId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLeaveHoursType(),
                request.getEmployeeNotes(),
                request.getCreatedAt(),
                request.getRequestStatus(),
                request.getReviewerNotes(),
                request.getTotalHours(),
                request.getEmployee().getName() + " " + request.getEmployee().getSurname()
        );
    }

    //METODO ASSIGNMENT TYPE
    private AssignmentType findAssignmentTypeForRequest(User employee, LocalDate date) {
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(employee, date);
        // O un valore di default se non c'è turno
        return optionalShift.map(ShiftAssignment::getAssignmentType).orElse(null);
    }

    //HELPER RISPOSTA ADMIN O SHIFT MANAGER
    public LeaveHoursRequestReviewerResponseDTO mapToReviewerResponse(LeaveHoursRequest request) {

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

        AssignmentType currentAssignmentType = findAssignmentTypeForRequest(request.getEmployee(), request.getDate());

        return new LeaveHoursRequestReviewerResponseDTO(
                request.getId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLeaveHoursType(),
                request.getEmployeeNotes(),
                request.getCreatedAt(),
                request.getResponseDate(),
                request.getRequestStatus(),
                reviewerNotesResponse,
                reviewerRoles,
                reviewer,
                currentAssignmentType,
                request.getEmployee().getName() + " " + request.getEmployee().getSurname(),
                request.getTotalHours()
        );
    }

    //HELPER ACCOUNT AUTENTICATO E' ADMIN O SHIFT MANAGER
    private void validateAdminAndShiftManagerRole(Account currentAccount) {

        if (currentAccount == null || currentAccount.getRoles() == null ||
                currentAccount.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()) || "SHIFT MANAGER".equals(role.getName()))) {


            throw new UnauthorizedException("Accesso negato: solo l'amministratore o il responsabile turni possono visualizzare queste richieste.");
        }
    }

    //FIND BY ID
    public LeaveHoursRequest findById(UUID requestId) {
        return leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));
    }

    //____________________________________

    //1. CREA NUOVA RICHIESTA

    public LeaveHoursRequestResponseDTO saveNewRequest(Account currentAccount, LeaveHoursRequestDTO payload) {
        // 1. Recuperiamo l'utente dall'account loggato
        User user = currentAccount.getUser();

        //2. controllo data
        if (payload.date().isBefore(LocalDate.now())) {
            throw new BadRequestException("La data selezionata non è valida. Seleziona una data futura");
        }

        // 3. Controllo se ha un contratto attivo in quel periodo
        boolean hasActiveContract = contractService.existsByUserAndContractStatus(user, ContractStatus.ACTIVE);
        if (!hasActiveContract) {
            throw new BadRequestException("Impossibile inoltrare la richiesta: l'utente non ha un contratto attivo.");
        }

        //4. Controllo se ha già mandato una richiesta per quella data
        if (leaveHoursRequestRepository.existsByDate(payload.date())) {
            throw new BadRequestException("Richiesta già inviata per la data selezionata");
        }

        //5.Creazione dell'entità
        LeaveHoursRequest request = new LeaveHoursRequest();
        request.setDate(payload.date());
        request.setStartTime(payload.startTime());
        request.setEndTime(payload.endTime());
        request.setEmployee(user);
        request.setLeaveHoursType(payload.leaveHoursType());
        request.setEmployeeNotes(payload.employeeNotes());

        //6. calcolo ore totali
        request.calculateTotalHours();


        //7. Impostiamo i campi di default della richiesta
        request.setRequestStatus(RequestStatus.SENT);
        request.setCreatedAt(LocalDateTime.now());

        //8. salvataggio
        LeaveHoursRequest updatedRequest = leaveHoursRequestRepository.save(request);

        //invio email

        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.buildLeaveRequest(recipientRole, request.getEmployee().getName(), request.getEmployee().getSurname(), request.getDate(), request.getStartTime(), request.getEndTime(), newRequestUrl);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova richiesta - YouRoster!]",
                    htmlBody);
        }

        //9.return
        return mapToResponse(updatedRequest);
    }


    //2. SE LA RICHIESTA E' SENT, CONSENTI MODIFICA
    public LeaveHoursRequestResponseDTO updateIfStatusIsSent(Account currentAccount, UpdateLeaveHoursRequestDTO payload, UUID requestId) {
        //1 trova la richiesta
        LeaveHoursRequest request = leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        //2 controlla status se sent passa
        if (request.getRequestStatus() == RequestStatus.SENT) {

            //controllo date, se è già passata
            if (request.getDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Non puoi modificare una richiesta relativa ad una data passata!");
            }

            if (payload.date() != null) {
                request.setDate(payload.date());
            }

            if (payload.leaveHoursType() != null) {
                request.setLeaveHoursType(payload.leaveHoursType());
            }

            if (payload.employeeNotes() != null) {
                request.setEmployeeNotes(payload.employeeNotes());
            }

            boolean timeChanged = false;

            if (payload.startTime() != null) {
                request.setStartTime(payload.startTime());
                timeChanged = true;
            }

            if (payload.endTime() != null) {
                request.setEndTime(payload.endTime());
                timeChanged = true;
            }

            // Ricalcoliamo le ore totali SOLO SE è stata modificata almeno uno degli orari
            if (timeChanged) {
                if (request.getStartTime().isAfter(request.getEndTime())) {
                    throw new BadRequestException("L'orario di inizio non può essere successivo all'orario di fine.");
                }
                request.calculateTotalHours();
            }
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }

        //salva
        LeaveHoursRequest updatedRequest = leaveHoursRequestRepository.save(request);

        //return
        return mapToResponse(updatedRequest);
    }


    //3. SE LA RICHIESTA E' SENT, CONSENTI ELIMINAZIONE
    public void deleteIfStatusIsSent(Account currentAccount, UUID requestId) {
        //1. trova la richiesta
        LeaveHoursRequest request = leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        //2. controlla che lo stato è sent, se si, elimina, altrimenti messaggio di errore
        if (request.getRequestStatus() == RequestStatus.SENT) {

            //controllo date, se è già passata
            if (request.getDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Non puoi eliminare una richiesta relativa ad una data passata!");
            }

            leaveHoursRequestRepository.delete(request);
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }
    }

    //4. GET MY REQUESTS
    public Page<LeaveHoursRequestReviewerResponseDTO> getMyRequests(Account currentAccount, LocalDate date, LocalTime startTime, LocalTime endTime, RequestStatus status, Pageable pageable) {
        Page<LeaveHoursRequest> requests;
        // Se l'utente ha inserito entrambi gli orari, filtro per range
        if (startTime != null && endTime != null) {
            requests = leaveHoursRequestRepository.findByEmployeeAndStartTimeBetween(currentAccount.getUser(), startTime, endTime, pageable);
        } else if (startTime != null) {
            requests = leaveHoursRequestRepository.findByEmployeeAndStartTime(currentAccount.getUser(), startTime, pageable);
        } else if (status != null) {
            requests = leaveHoursRequestRepository.findByEmployeeAndRequestStatus(currentAccount.getUser(), status, pageable);
        } else {
            // Altrimenti prendiamo tutte le sue richieste
            requests = leaveHoursRequestRepository.findByEmployee(currentAccount.getUser(), pageable);
        }
        //map
        return requests.map(this::mapToReviewerResponse);
    }


    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    public Page<LeaveHoursRequestReviewerResponseDTO> getAllWithFilters(Account currentAccount, String name, RequestStatus status, LocalDate date, LocalTime startTime, LocalTime endTime, Pageable pageable) {
        // 1. Controllo di sicurezza
        validateAdminAndShiftManagerRole(currentAccount);

        //2. Esecuzione query con la stringa di ricerca (nome o cognome)
        return leaveHoursRequestRepository.findWithFilters(name, status, date, startTime, endTime, pageable).map(this::mapToReviewerResponse);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    public Page<LeaveHoursRequestResponseDTO> getPendingRequests(
            Account currentAccount,
            String name,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Pageable pageable) {

        validateAdminAndShiftManagerRole(currentAccount);

        boolean isShiftManager = currentAccount.getRoles().stream()
                .anyMatch(role -> "SHIFT MANAGER".equals(role.getName()));

        if (isShiftManager) {
            return leaveHoursRequestRepository
                    .findPendingRequestsForShiftManager(
                            name,
                            RequestStatus.SENT,
                            currentAccount.getUser(),
                            date,
                            startTime,
                            endTime,
                            pageable
                    )
                    .map(this::mapToResponse);
        }

        return leaveHoursRequestRepository
                .findWithFilters(
                        name,
                        RequestStatus.SENT,
                        date,
                        startTime,
                        endTime,
                        pageable
                )
                .map(this::mapToResponse);
    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED" + INVIO EMAIL A SHIFT MANAGER, ADMIN
    //+ CAMBIA STATO DELLO SHIFT ASSIGNMENT, TORNI WORK.

    public LeaveHoursRequestReviewerResponseDTO cancelRequest(Account currentAccount, UUID requestId) {
        // 1. Trova la richiesta
        LeaveHoursRequest request = leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        User employee = currentAccount.getUser();

        // 2. Verifica che la richiesta sia effettivamente in stato APPROVED
        if (request.getRequestStatus() != RequestStatus.APPROVED) {
            throw new BadRequestException("Puoi annullare solo una richiesta che era stata precedentemente approvata.");
        }

        // 3. Cambia lo stato in CANCELLED
        request.setRequestStatus(RequestStatus.CANCELLED);

        // 4. RIPRISTINO TURNI: Siccome erano ON_HOLIDAY, ora devono tornare a WORK!
        List<ShiftAssignment> shiftsToUpdate = shiftAssignmentService.findByUserAndShiftDateAndShiftStartTimeAndShiftEndTime(request.getEmployee(), request.getDate(), request.getStartTime(), request.getEndTime());


        for (ShiftAssignment shift : shiftsToUpdate) {
            shift.setAssignmentType(AssignmentType.WORK); // <-- Torna a lavoro!
            shiftAssignmentService.save(shift);
        }

        LeaveHoursRequest savedRequest = leaveHoursRequestRepository.save(request);

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
    public LeaveHoursRequestReviewerResponseDTO approveRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Trova la richiesta
        LeaveHoursRequest request = leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        // 3. Verifica che la richiesta sia effettivamente in stato SENT (quindi ancora da valutare)
        if (request.getRequestStatus() != RequestStatus.SENT) {
            throw new BadRequestException("Questa richiesta è già stata valutata o non è in uno stato valido per la valutazione.");
        }

        //4. IMPOSTA REVISORE SOLO SE ESISTE UNO USER ASSOCIATO
        if (currentAccount.getUser() != null) {
            request.setReviewer(currentAccount.getUser());
        }

        if (payload != null && payload.notes() != null) {
            request.setReviewerNotes(payload.notes());
        }

        request.setRequestStatus(RequestStatus.APPROVED);
        request.setResponseDate(LocalDate.now());


        // ------- CAMBIO TIPO SHIFT ASSIGNMENT
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(
                request.getEmployee(),
                request.getDate()
        );

        ShiftAssignment shiftToUpdate;

        if (optionalShift.isPresent()) {
            shiftToUpdate = optionalShift.get();
        } else {
            // Caso B: l'assegnazione non esiste, la creiamo
            shiftToUpdate = new ShiftAssignment();
            shiftToUpdate.setUser(request.getEmployee());
            shiftToUpdate.setShiftDate(request.getDate());
        }

        // Ora impostiamo l'AssignmentType e salviamo
        shiftToUpdate.setAssignmentType(AssignmentType.PERMISSION);
        shiftToUpdate.setShift(null);

        shiftAssignmentService.save(shiftToUpdate);

        //3. salva
        LeaveHoursRequest updatedRequest = leaveHoursRequestRepository.save(request);

        //invio email

        String htmlBody = EmailTemplateBuilder.buildResponse(request.getEmployee().getName(), urlDashboard);

        emailService.sendHtmlEmail(request.getEmployee().getAccount().getEmail(),
                "[Richiesta approvata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);

    }

    //9. ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    public LeaveHoursRequestReviewerResponseDTO rejectRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        //1. validazione
        validateAdminAndShiftManagerRole(currentAccount);

        //2. trova la richiesta
        LeaveHoursRequest request = leaveHoursRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

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

        if (payload != null && payload.notes() != null) {
            request.setReviewerNotes(payload.notes());
        }

        // 5. Salva
        LeaveHoursRequest updatedRequest = leaveHoursRequestRepository.save(request);

        //invio email
        String htmlBody = EmailTemplateBuilder.buildResponse(request.getEmployee().getName(), urlDashboard);

        emailService.sendHtmlEmail(request.getEmployee().getAccount().getEmail(),
                "[Richiesta rifiutata - YouRoster!]",
                htmlBody);
        return mapToReviewerResponse(updatedRequest);
    }

}
