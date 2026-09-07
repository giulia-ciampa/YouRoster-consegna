package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ChangeHolidayRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeHolidayRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeHolidayRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ChangeHolidayRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChangeHolidayRequestService {
    private final ChangeHolidayRequestRepository changeHolidayRequestRepository;
    private final HolidayRequestService holidayRequestService;
    private final AccountService accountService;
    private final EmailService emailService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftService shiftService;
    //LINK
    @Value("${newChangeHolidayRequest.url}")
    private String newChangeRequestUrl;

    @Value("${response.url}")
    private String urlUserDashBoard;


    public ChangeHolidayRequestService(ChangeHolidayRequestRepository changeHolidayRequestRepository, HolidayRequestService holidayRequestService, AccountService accountService, EmailService emailService, ShiftAssignmentService shiftAssignmentService, ShiftService shiftService) {
        this.changeHolidayRequestRepository = changeHolidayRequestRepository;
        this.holidayRequestService = holidayRequestService;
        this.accountService = accountService;
        this.emailService = emailService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.shiftService = shiftService;
    }

    //HELPER DTO DI RISPOSTA
    public ChangeHolidayRequestResponseDTO mapToResponse(ChangeHolidayRequest request) {

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

        return new ChangeHolidayRequestResponseDTO(
                request.getId(),
                request.getOriginalRequest().getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getEmployeeNotes(),
                request.getOriginalRequest().getEmployeeNotes(),
                ((HolidayRequest) request.getOriginalRequest()).getStartDate(),
                ((HolidayRequest) request.getOriginalRequest()).getEndDate(),
                request.getCreatedAt(),
                request.getOriginalRequest().getCreatedAt(),
                request.getTotalDays(),
                ((HolidayRequest) request.getOriginalRequest()).getTotalDaysRequested(),
                request.getChangeRequestStatus(),
                request.getReviewCount(),
                reviewerName,
                request.getResponseDate(),
                request.getOriginalRequest().getResponseDate(),
                request.getOriginalRequest().getEmployee().getName() + " " + request.getOriginalRequest().getEmployee().getSurname(),
                request.getReviewerNotes(),
                request.getOriginalRequest().getReviewerNotes());

    }

    //METODO ASSIGNMENT TYPE
    private AssignmentType findAssignmentTypeForRequest(User employee, LocalDate date) {
        Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(employee, date);
        // O un valore di default se non c'è turno
        return optionalShift.map(ShiftAssignment::getAssignmentType).orElse(null);
    }

    //HELPER DTO DI RISPOSTA REVIEWER
    public ChangeHolidayRequestReviewerResponseDTO mapToReviewerResponse(ChangeHolidayRequest request) {

        // 1. Gestione le note del reviewer: se non è ancora stata revisionata o il reviewer è null, mettiamo il messaggio di default
        String reviewerNotesResponse;
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {
            reviewerNotesResponse = "La richiesta non è ancora stata revisionata";
        } else {
            reviewerNotesResponse = request.getReviewerNotes() != null ? request.getReviewerNotes() : "Nessuna nota inserita";
        }


        // 3. Recuperiamo l'AssignmentType per la data di inizio (o gestiscilo come preferisci)
        AssignmentType currentAssignmentType = null;
        Optional<ShiftAssignment> assignmentOpt = shiftAssignmentService.findByUserAndShiftDateOptional(request.getOriginalRequest().getEmployee(), request.getStartDate());
        if (assignmentOpt.isPresent()) {
            currentAssignmentType = assignmentOpt.get().getAssignmentType();
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


        return new ChangeHolidayRequestReviewerResponseDTO(
                request.getId(),
                request.getOriginalRequest().getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getEmployeeNotes(),
                request.getOriginalRequest().getEmployeeNotes(),
                ((HolidayRequest) request.getOriginalRequest()).getStartDate(),
                ((HolidayRequest) request.getOriginalRequest()).getEndDate(),
                request.getCreatedAt(),
                request.getOriginalRequest().getCreatedAt(),
                request.getChangeRequestStatus(),
                request.getReviewCount(),
                request.getResponseDate(),
                request.getOriginalRequest().getResponseDate(),
                reviewerNotesResponse,
                reviewerName,
                currentAssignmentType,
                request.getTotalDays(),
                ((HolidayRequest) request.getOriginalRequest()).getTotalDaysRequested(),
                request.getOriginalRequest().getReviewerNotes()
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
    public ChangeHolidayRequestResponseDTO saveChangeRequest(Account currentAccount, UUID originalRequestId, ChangeHolidayRequestDTO payload) {
        User userAutenticato = currentAccount.getUser();

        //1. recupera richiesta originale
        HolidayRequest originalRequest = holidayRequestService.findById(originalRequestId);


        //2. vedi lo stato
        if (originalRequest.getRequestStatus() == RequestStatus.SENT) {
            throw new BadRequestException("Non puoi richiedere una modifica per una richiesta ancora in attesa di approvazione.");
        }

        //controllo se esiste già una richiesta di modifica per quella richiesta
        boolean exists =
                changeHolidayRequestRepository
                        .existsByOriginalRequest_IdAndChangeRequestStatus(
                                originalRequestId,
                                RequestStatus.SENT
                        );
        if (exists) {
            throw new BadRequestException("Hai già inviato una richiesta di modifica. Presto verrà valutata.");
        }

        //3. compila campi nuova richiesta
        ChangeHolidayRequest changeRequest = new ChangeHolidayRequest(
                originalRequest,
                payload.employeeNotes(),
                payload.startDate(),
                payload.endDate()
        );

        //4. salva
        ChangeHolidayRequest savedChangeRequest = changeHolidayRequestRepository.save(changeRequest);

        //5. invio email
        //invio email
        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.buildChangeHolidayRequest(recipientRole, currentAccount.getUser().getName(), currentAccount.getUser().getSurname(), changeRequest.getStartDate(), changeRequest.getEndDate(), newChangeRequestUrl);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova richiesta di modifica - YouRoster!]",
                    htmlBody);
        }
        //6. return
        return mapToResponse(savedChangeRequest);
    }


    //2. MODIFICA RICHIESTA DI MODIFICA IF STATUS SENT
    public ChangeHolidayRequestResponseDTO updateRequestIfNotSent(Account currentAccount, ChangeHolidayRequestDTO payload, UUID requestId) {
        ChangeHolidayRequest request = changeHolidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        //2 controlla status se sent passa
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {

            boolean datesChanged = false;

            if (payload.startDate() != null) {
                request.setStartDate(payload.startDate());
                datesChanged = true;
            }

            if (payload.endDate() != null) {
                request.setEndDate(payload.endDate());
                datesChanged = true;
            }

            // Ricalcoliamo i giorni totali SOLO SE è stata modificata almeno una delle date
            if (datesChanged) {
                if (request.getStartDate().isAfter(request.getEndDate())) {
                    throw new BadRequestException("La data di inizio non può essere successiva alla data di fine.");
                }
                request.calculateTotalDays();
            }

            if (payload.employeeNotes() != null) {
                request.setEmployeeNotes(payload.employeeNotes());
            }
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }

        //3 salva
        ChangeHolidayRequest savedRequest = changeHolidayRequestRepository.save(request);

        //return
        return mapToResponse(savedRequest);

    }

    //3. ELIMINA RICHIESTA DI MODIFICA IF STATUS NON E' SENT
    public void deleteIfStatusIsNotSent(Account currentAccount, UUID requestId) {
        //1. trova la richiesta
        ChangeHolidayRequest request = changeHolidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Richiesta non trovata"));

        //2. controlla che lo stato è sent, se si, elimina, altrimenti messaggio di errore
        if (request.getChangeRequestStatus() == RequestStatus.SENT) {
            changeHolidayRequestRepository.delete(request);
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }
    }

    //4. GET MY REQUESTS
    public Page<ChangeHolidayRequestReviewerResponseDTO> getMyRequests(Account currentAccount, LocalDate startDate, LocalDate endDate, RequestStatus status, Pageable pageable) {
        Page<ChangeHolidayRequest> requests;

        // Se l'utente ha inserito entrambe le date, filtro per range
        if (startDate != null && endDate != null) {
            requests = changeHolidayRequestRepository.findByOriginalRequest_EmployeeAndStartDateBetween(currentAccount.getUser(), startDate, endDate, pageable);
        } else if (startDate != null) {
            requests = changeHolidayRequestRepository.findByOriginalRequest_EmployeeAndStartDate(currentAccount.getUser(), startDate, pageable);
        } else if (status != null) {
            requests = changeHolidayRequestRepository.findByOriginalRequest_EmployeeAndChangeRequestStatus(currentAccount.getUser(), status, pageable);
        } else {
            // Altrimenti prendiamo tutte le sue richieste
            requests = changeHolidayRequestRepository.findByOriginalRequest_Employee(currentAccount.getUser(), pageable);
        }

        return requests.map(this::mapToReviewerResponse);
    }

    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    public Page<ChangeHolidayRequestReviewerResponseDTO> getAllWithFilters(Account currentAccount, String name, RequestStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 1. Controllo di sicurezza
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Esecuzione query con la stringa di ricerca (nome o cognome)
        return changeHolidayRequestRepository.findWithFilters(name, status, startDate, endDate, pageable).map(this::mapToReviewerResponse);
    }


    //6. GET REQUEST DA LAVORARE (SENT)
    public Page<ChangeHolidayRequestResponseDTO> getPendingRequests(
            Account currentAccount,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        validateAdminAndShiftManagerRole(currentAccount);

        boolean isShiftManager = currentAccount.getRoles().stream()
                .anyMatch(role -> "SHIFT MANAGER".equals(role.getName()));

        if (isShiftManager) {
            return changeHolidayRequestRepository
                    .findPendingRequestsForShiftManager(
                            name,
                            RequestStatus.SENT,
                            currentAccount.getUser(),
                            startDate,
                            endDate,
                            pageable
                    )
                    .map(this::mapToResponse);
        }

        return changeHolidayRequestRepository
                .findWithFilters(
                        name,
                        RequestStatus.SENT,
                        startDate,
                        endDate,
                        pageable
                )
                .map(this::mapToResponse);
    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED" + INVIO EMAIL A SHIFT MANAGER, ADMIN
    //+ CAMBIA STATO DELLO SHIFT ASSIGNMENT, TORNI WORK.
    public ChangeHolidayRequestReviewerResponseDTO cancelRequest(Account currentAccount, UUID requestId) {
        // 1. Trova la richiesta

        ChangeHolidayRequest request = changeHolidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


        //2 Verifica che la richiesta appartenga all'utente autenticato
        if (!request.getOriginalRequest()
                .getEmployee().getId()
                .equals(currentAccount.getUser().getId())) {
            System.out.println(currentAccount.getUser().getId());
            System.out.println(request.getOriginalRequest().getEmployee().getId());
            throw new BadRequestException(
                    "Non puoi annullare una richiesta di modifica che non ti appartiene."
            );
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
        ChangeHolidayRequest savedRequest = changeHolidayRequestRepository.save(request);


        //6. invio email a shift manager
        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.buildCancelledHolidayRequest(recipientRole, currentAccount.getUser().getName(), currentAccount.getUser().getSurname(), request.getStartDate(), request.getEndDate());

            emailService.sendHtmlEmail(recipientEmail,
                    "[Richiesta annullata - YouRoster!]",
                    htmlBody);
        }

        // 9. Ritorno della risposta
        return mapToReviewerResponse(savedRequest);

    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    public ChangeHolidayRequestReviewerResponseDTO approveRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Trova la richiesta
        ChangeHolidayRequest request = changeHolidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


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

        //------- CAMBIO TIPO / CREAZIONE SHIFT ASSIGNMENT PER INTERO RANGE

        LocalDate current = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        AssignmentType newType = AssignmentType.ON_HOLIDAY; // O il tipo corretto in base alla logica di modifica

        while (!current.isAfter(endDate)) {
            // Controlliamo se esiste già un'assegnazione per questa specifica data
            Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(
                    request.getOriginalRequest().getEmployee(),
                    current
            );

            ShiftAssignment shiftToUpdate;
            if (optionalShift.isPresent()) {
                shiftToUpdate = optionalShift.get();
            } else {
                shiftToUpdate = new ShiftAssignment();
                shiftToUpdate.setUser(request.getOriginalRequest().getEmployee());
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

        // 5. Salva

        ChangeHolidayRequest updatedRequest = changeHolidayRequestRepository.save(request);

        //invio email

        String htmlBody = EmailTemplateBuilder.buildResponse(request.getOriginalRequest().getEmployee().getName(), urlUserDashBoard);

        emailService.sendHtmlEmail(request.getOriginalRequest().getEmployee().getAccount().getEmail(),
                "[Richiesta approvata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);

    }


    //9. ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    public ChangeHolidayRequestReviewerResponseDTO rejectRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Trova la richiesta
        ChangeHolidayRequest request = changeHolidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


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
        ChangeHolidayRequest updatedRequest = changeHolidayRequestRepository.save(request);

        //invio email
        String htmlBody = EmailTemplateBuilder.buildResponse(request.getOriginalRequest().getEmployee().getName(), urlUserDashBoard);

        emailService.sendHtmlEmail(request.getOriginalRequest().getEmployee().getAccount().getEmail(),
                "[Richiesta rifiutata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);
    }

    //GET SINGOLA RICHIESTA
    public ChangeHolidayRequestResponseDTO getMyChangeRequest(
            Account currentAccount,
            UUID originalRequestId
    ) {

        ChangeHolidayRequest request =
                changeHolidayRequestRepository
                        .findByOriginalRequest_IdAndOriginalRequest_Employee(
                                originalRequestId,
                                currentAccount.getUser()
                        )
                        .orElseThrow(() ->
                                new NotFoundException("Nessuna richiesta di modifica trovata")
                        );

        return mapToResponse(request);
    }

    //GET SINGOLA RICHIESTA PER SHIFT MANAGER E ADMIN
    public ChangeHolidayRequestResponseDTO getChangeRequestForReviewer(UUID requestId) {


        ChangeHolidayRequest request =
                changeHolidayRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Nessuna richiesta di modifica trovata"
                                )
                        );

        return mapToResponse(request);
    }

}
