package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.HolidayRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateHolidayRequestDTO;
import giuliaciampa.YouRoster.dto.responses.HolidayRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.HolidayRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.HolidayRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HolidayRequestService {

    private final HolidayRequestRepository holidayRequestRepository;
    private final ContractService contractService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final EmailService emailService;
    private final AccountService accountService;
    private final ShiftService shiftService;
    //LINK
    @Value("${newHolidayRequest.url}")
    private String newRequestUrl;

    @Value("${response.url}")
    private String urlUserDashBoard;

    public HolidayRequestService(HolidayRequestRepository holidayRequestRepository, ContractService contractService, ShiftAssignmentService shiftAssignmentService, EmailService emailService, AccountService accountService, ShiftService shiftService) {
        this.holidayRequestRepository = holidayRequestRepository;
        this.contractService = contractService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.emailService = emailService;
        this.accountService = accountService;
        this.shiftService = shiftService;
    }

    //HELPER DI RISPOSTA ALLA RICHIESTA
    public HolidayRequestResponseDTO mapToResponse(HolidayRequest request) {


        return new HolidayRequestResponseDTO(
                request.getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getEmployeeNotes(),
                request.getCreatedAt(),
                request.getRequestStatus(),
                request.getReviewerNotes(),
                request.getTotalDaysRequested(),
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
    private HolidayRequestReviewerResponseDTO mapToReviewerResponse(HolidayRequest request) {

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
        //________________________

        return new HolidayRequestReviewerResponseDTO(
                request.getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getEmployeeNotes(),
                request.getCreatedAt(),
                request.getRequestStatus(),
                request.getResponseDate(),
                reviewerNotesResponse,
                reviewer,
                reviewerRoles,
                currentAssignmentType,
                request.getEmployee().getName() + " " + request.getEmployee().getSurname(),
                request.getTotalDaysRequested()

        );
    }

    //HELPER ACCOUNT AUTENTICATO E' ADMIN O SHIFT MANAGER
    private void validateAdminAndShiftManagerRole(Account currentAccount) {

        if (currentAccount == null || currentAccount.getRoles() == null ||
                currentAccount.getRoles().stream().noneMatch(role -> "ADMIN".equals(role.getName()) || "SHIFT MANAGER".equals(role.getName()))) {


            throw new UnauthorizedException("Accesso negato: solo l'amministratore o il responsabile turni possono visualizzare queste richieste.");
        }
    }

    //METOOD PER PAYROLL SERVICE E LEAVESUMMARYSERVICE
    public List<HolidayRequest> findByEmployeeAndRequestStatusAndStartDateBetween(User employee, RequestStatus status, LocalDate startDate, LocalDate endDate) {
        return holidayRequestRepository.findByEmployeeAndRequestStatusAndStartDateBetween(employee, status, startDate, endDate);
    }

    //METODO FIND BY ID
    public HolidayRequest findById(UUID requestId) {
        return holidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Richiesta non trovata"));
    }

    //______________________________________________________________________________________________________

    //1. CREA NUOVA RICHIESTA
    public HolidayRequestResponseDTO saveHolidayRequest(Account currentAccount, HolidayRequestDTO payload) {
        // 1. Recuperiamo l'utente dall'account loggato
        User user = currentAccount.getUser();

        // 2. Controllo date logiche
        if (payload.startDate().isAfter(payload.endDate())) {
            throw new BadRequestException("La data di inizio non può essere successiva alla data di fine.");
        }

        // 3. Controllo se ha un contratto attivo in quel periodo
        boolean hasActiveContract = contractService.existsByUserAndContractStatus(user, ContractStatus.ACTIVE);
        if (!hasActiveContract) {
            throw new BadRequestException("Impossibile inoltrare la richiesta: l'utente non ha un contratto attivo.");
        }

        //controllo se ha già mandato una richiesta per quelle date
        if (holidayRequestRepository.existsByStartDateAndEndDate(payload.startDate(), payload.endDate())) {
            throw new BadRequestException("La richiesta per le date indicate è già stata inviata");
        }

        // 4. Creazione dell'entità Ferie (o giorni_ferie)
        HolidayRequest request = new HolidayRequest();
        request.setEmployee(user);
        request.setStartDate(payload.startDate());
        request.setEndDate(payload.endDate());

        //calcolo giorni totali
        request.calculateTotalDays();

        // Impostiamo i campi di default della richiesta
        request.setRequestStatus(RequestStatus.SENT);
        request.setCreatedAt(LocalDateTime.now());
        request.setEmployeeNotes(payload.employeeNotes());

        // 5. Salvataggio
        HolidayRequest savedRequest = holidayRequestRepository.save(request);

        //invio email
        List<String> rolesToNotify = List.of("SHIFT MANAGER", "ADMIN");

        List<Account> recipients = accountService.findByRolesNameIn(rolesToNotify);

        for (Account recipient : recipients) {
            String recipientEmail = recipient.getEmail();

            String recipientRole = recipient.getRoles().stream()
                    .map(Role::getName)
                    .findFirst()
                    .orElse("Responsabile");

            String htmlBody = EmailTemplateBuilder.buildHolidayRequest(recipientRole, request.getEmployee().getName(), request.getEmployee().getSurname(), request.getStartDate(), request.getEndDate(), newRequestUrl);

            emailService.sendHtmlEmail(recipientEmail,
                    "[Nuova richiesta - YouRoster!]",
                    htmlBody);
        }
        // 6. Mapping e ritorno del DTO di risposta
        return mapToResponse(savedRequest);
    }


    //2. SE LA RICHIESTA E' SENT, CONSENTI MODIFICA
    public HolidayRequestResponseDTO updateIfStatusIsSent(Account currentAccount, UpdateHolidayRequestDTO payload, UUID requestId) {
        //1 trova la richiesta
        HolidayRequest request = holidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


        //2 controlla status se sent passa
        if (request.getRequestStatus() == RequestStatus.SENT) {

            //controllo date, se è già passata
            if (request.getStartDate().isBefore(LocalDate.now()) || request.getEndDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Non puoi modificare una richiesta relativa ad una data passata!");
            }

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

        HolidayRequest savedRequest = holidayRequestRepository.save(request);

        //return

        return mapToResponse(savedRequest);
    }

    //3. SE LA RICHIESTA E' SENT, CONSENTI ELIMINAZIONE
    public void DeleteIfStatusIsSent(Account currentAccount, UUID requestId) {
        //1. trova la richiesta
        HolidayRequest request = holidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));

        //2. controlla che lo stato è sent, se si, elimina, altrimenti messaggio di errore
        if (request.getRequestStatus() == RequestStatus.SENT) {
            //controllo date, se è già passata
            if (request.getStartDate().isBefore(LocalDate.now()) || request.getEndDate().isBefore(LocalDate.now())) {
                throw new BadRequestException("Non puoi eliminare una richiesta relativa ad una data passata!");
            }
            holidayRequestRepository.delete(request);
        } else {
            throw new BadRequestException("La richiesta è già stata presa in carico. Apri una nuova pratica di rettifica.");
        }

    }

    //4. GET MY REQUESTS
    public Page<HolidayRequestReviewerResponseDTO> getMyRequests(Account currentAccount, LocalDate startDate, LocalDate endDate, RequestStatus status, Pageable pageable) {
        Page<HolidayRequest> requests;

        // Se l'utente ha inserito entrambe le date, filtro per range
        if (startDate != null && endDate != null) {
            requests = holidayRequestRepository.findByEmployeeAndStartDateBetween(currentAccount.getUser(), startDate, endDate, pageable);
        } else if (startDate != null) {
            requests = holidayRequestRepository.findByEmployeeAndStartDate(currentAccount.getUser(), startDate, pageable);
        } else if (status != null) {
            requests = holidayRequestRepository.findByEmployeeAndRequestStatus(currentAccount.getUser(), status, pageable);
        } else {
            // Altrimenti prendiamo tutte le sue richieste
            requests = holidayRequestRepository.findByEmployee(currentAccount.getUser(), pageable);
        }

        //map
        return requests.map(this::mapToReviewerResponse);
    }


    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    public Page<HolidayRequestReviewerResponseDTO> getAllWithFilters(Account currentAccount, String name, RequestStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        // 1. Controllo di sicurezza
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Esecuzione query con la stringa di ricerca (nome o cognome)
        return holidayRequestRepository.findWithFilters(name, status, startDate, endDate, pageable).map(this::mapToReviewerResponse);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    public Page<HolidayRequestResponseDTO> getPendingRequests(
            Account currentAccount,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        validateAdminAndShiftManagerRole(currentAccount);

        boolean isShiftManager = currentAccount.getRoles().stream()
                .anyMatch(role -> "SHIFT MANAGER".equals(role.getName()));

        if (isShiftManager) {
            return holidayRequestRepository
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

        return holidayRequestRepository
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

    public HolidayRequestReviewerResponseDTO cancelRequest(Account currentAccount, UUID requestId) {
        // 1. Trova la richiesta
        HolidayRequest request = holidayRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata."));

        User employee = currentAccount.getUser();

        // 2. Verifica che la richiesta sia effettivamente in stato APPROVED
        if (request.getRequestStatus() != RequestStatus.APPROVED) {
            throw new BadRequestException("Puoi annullare solo una richiesta che era stata precedentemente approvata.");
        }

        // 3. Cambia lo stato in CANCELLED
        request.setRequestStatus(RequestStatus.CANCELLED);

        // 4. RIPRISTINO TURNI: Siccome erano ON_HOLIDAY, ora devono tornare a WORK!
        List<ShiftAssignment> shiftsToUpdate = shiftAssignmentService.findByUserAndShiftDateBetween(
                request.getEmployee(),
                request.getStartDate(),
                request.getEndDate()
        );

        for (ShiftAssignment shift : shiftsToUpdate) {
            shift.setAssignmentType(AssignmentType.WORK); // <-- Torna a lavoro!
            shiftAssignmentService.save(shift);
        }

        // 5. Salvataggio della richiesta
        HolidayRequest savedRequest = holidayRequestRepository.save(request);

        // 6. invio email a shift manager
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

        // 7. Ritorno della risposta
        return mapToReviewerResponse(savedRequest);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    public HolidayRequestReviewerResponseDTO approveRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Trova la richiesta
        HolidayRequest request = holidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


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

        if (payload != null && payload.notes() != null) {
            request.setReviewerNotes(payload.notes());
        }
        //------- CAMBIO TIPO / CREAZIONE SHIFT ASSIGNMENT PER INTERO RANGE

        LocalDate current = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        AssignmentType newType = AssignmentType.ON_HOLIDAY;

        while (!current.isAfter(endDate)) {
            // Controlliamo se esiste già un'assegnazione per questa specifica data
            Optional<ShiftAssignment> optionalShift = shiftAssignmentService.findByUserAndShiftDateOptional(request.getEmployee(), current);

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
            System.out.println(
                    ">>> ASSEGNAZIONE FERIE SALVATA: "
                            + shiftToUpdate.getUser().getName()
                            + " | "
                            + shiftToUpdate.getShiftDate()
                            + " | "
                            + shiftToUpdate.getAssignmentType()
            );

            current = current.plusDays(1);
        }

        //-------------------------

        // 5. Salva

        HolidayRequest updatedRequest = holidayRequestRepository.save(request);

        //invio email

        String htmlBody = EmailTemplateBuilder.buildResponse(request.getEmployee().getName(), urlUserDashBoard);

        emailService.sendHtmlEmail(request.getEmployee().getAccount().getEmail(),
                "[Richiesta approvata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);
    }

    //9 ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    public HolidayRequestReviewerResponseDTO rejectRequest(ReviewerNotesDTO payload, Account currentAccount, UUID requestId) {
        validateAdminAndShiftManagerRole(currentAccount);

        // 2. Trova la richiesta
        HolidayRequest request = holidayRequestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("La richiesta non è stata trovata"));


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
        HolidayRequest updatedRequest = holidayRequestRepository.save(request);

        //invio email
        String htmlBody = EmailTemplateBuilder.buildResponse(request.getEmployee().getName(), urlUserDashBoard);

        emailService.sendHtmlEmail(request.getEmployee().getAccount().getEmail(),
                "[Richiesta rifiutata - YouRoster!]",
                htmlBody);

        return mapToReviewerResponse(updatedRequest);
    }


}
