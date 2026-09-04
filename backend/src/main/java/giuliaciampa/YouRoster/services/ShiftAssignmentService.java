package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftAssignmentResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.repositories.ShiftAssignmentRepository;
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
public class ShiftAssignmentService {
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final UserService userService;
    private final ShiftService shiftService;
    private final EmailService emailService;
    @Value("${shiftUpdated.url}")
    private String userPageUrl;

    public ShiftAssignmentService(ShiftAssignmentRepository shiftAssignmentRepository, UserService userService, ShiftService shiftService, EmailService emailService) {
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.userService = userService;
        this.shiftService = shiftService;
        this.emailService = emailService;
    }

    //FIND BY ID
    public ShiftAssignment findById(UUID id) {
        return shiftAssignmentRepository.findById(id).orElseThrow(() -> new NotFoundException("L'assegnazione del turno non è stata trovata"));
    }

    //FIND BY USER ID AND SHIFT DATE
    public ShiftAssignment findByUserIdAndShiftDate(UUID userId, LocalDate shiftDate) {
        // 1. Verifichiamo che l'utente esista
        userService.findById(userId);

        // 2. Cerchiamo l'assegnazione e restituiamo
        return shiftAssignmentRepository.findByUserIdAndShiftDate(userId, shiftDate)
                .orElseThrow(() -> new NotFoundException("Nessuna assegnazione trovata nella data odierna."));
    }

    //FIND BY USER AND SHIFT DATE
    public ShiftAssignment findByUserAndShiftDate(User user, LocalDate shiftDate) {
        userService.findById(user.getId());

        return shiftAssignmentRepository.findByUserAndShiftDate(user, shiftDate).orElseThrow(() -> new NotFoundException("Nessuna assegnazione trovata nella data odierna."));
    }

    //OPTIONAL, FIND BY USER AND SHIFT DATE
    public Optional<ShiftAssignment> findByUserAndShiftDateOptional(User user, LocalDate shiftDate) {
        return shiftAssignmentRepository.findOptionalByUserAndShiftDate(user, shiftDate);
    }

    //FIND BY USER AND SHIFT DATE BETEEN
    public List<ShiftAssignment> findByUserAndShiftDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        return shiftAssignmentRepository.findByUserAndShiftDateBetween(user, startDate, endDate);
    }

    //SAVE
    public ShiftAssignment save(ShiftAssignment shift) {
        return shiftAssignmentRepository.save(shift);
    }

    //SHIFT ASSIGNMENT FIND BT EMPLOYEE, DATE, START TIME AND END TIME
    public List<ShiftAssignment> findByUserAndShiftDateAndShiftStartTimeAndShiftEndTime(User employee,
                                                                                        LocalDate date,
                                                                                        LocalTime shiftStartTime,
                                                                                        LocalTime shiftEndTime) {
        return shiftAssignmentRepository.findByUserAndShiftDateAndShiftStartTimeAndShiftEndTime(employee, date, shiftStartTime, shiftEndTime);
    }

    //HELPER
    public ShiftAssignmentResponseDTO toResponseDTO(ShiftAssignment assignment) {
        return new ShiftAssignmentResponseDTO(
                assignment.getId(),
                (assignment.getUser() != null) ? assignment.getUser().getId() : null,
                (assignment.getUser() != null) ? assignment.getUser().getAccount().getRoles().stream().map(Role::getName).toList() : null,
                (assignment.getUser() != null) ? assignment.getUser().getName() : null,
                (assignment.getUser() != null) ? assignment.getUser().getSurname() : null,
                (assignment.getUser() != null) ? assignment.getUser().getAccount().getEmail() : null,
                (assignment.getShift() != null && assignment.getShift().getOffice() != null) ? assignment.getShift().getOffice().getName() : null,
                (assignment.getShift() != null) ? assignment.getShift().getStartTime() : null,
                (assignment.getShift() != null) ? assignment.getShift().getEndTime() : null,
                assignment.getShiftDate(),
                assignment.getAssignmentType(),
                assignment.getTasks().stream()
                        .map(shiftTask -> shiftTask.getTaskTitle().getTitle())
                        .toList(),
                assignment.getShift() != null ? assignment.getShift().getId() : null);


    }

    //1 CREA UN'ASSEGNAZIONE
    public ShiftAssignmentResponseDTO saveNewAssignment(ShiftAssignmentDTO payload) {
        // 1. Recupero l'utente
        User user = userService.findById(payload.userId());

        Shift shift = null;

        // 2. Gestione del turno in base all'AssignmentType
        if (payload.assignmentType() == AssignmentType.WORK) {

            // Per WORK lo shift è obbligatorio
            if (payload.shiftId() == null) {
                throw new BadRequestException(
                        "L'assegnazione del turno è obbligatoria per i giorni di lavoro."
                );
            }

            // Recupero il turno
            shift = shiftService.findById(payload.shiftId());

            // Verifico che il turno sia attivo
            if (!shift.isActive()) {
                throw new BadRequestException(
                        "Non si può assegnare un turno disattivato."
                );
            }
        }

        // Per OFF / HOLIDAY / SICK /:
        // shift rimane null

        //controllo sulla possibilità di creazione di un'assegnazione fino al giorno prima
        if (!payload.shiftDate().isAfter(LocalDate.now())) {
            throw new BadRequestException(
                    "Non è possibile creare un'assegnazione per oggi o per una data passata."
            );
        }

        // 3. Controllo assegnazione già presente
        boolean alreadyAssigned =
                shiftAssignmentRepository.existsByUserAndShiftDate(
                        user,
                        payload.shiftDate()
                );

        if (alreadyAssigned) {
            throw new BadRequestException(
                    "Esiste già un'assegnazione per questo utente in questa data."
            );
        }


        // 4. Creazione assegnazione
        ShiftAssignment assignment = new ShiftAssignment();

        assignment.setUser(user);
        assignment.setShift(shift);
        assignment.setAssignmentType(payload.assignmentType());
        assignment.setShiftDate(payload.shiftDate());

        // 5. Salvataggio
        ShiftAssignment savedAssignment =
                shiftAssignmentRepository.save(assignment);

        System.out.println("SALVATO: " + savedAssignment.getId());
        System.out.println("SHIFT: " + savedAssignment.getShift());
        System.out.println("TYPE: " + savedAssignment.getAssignmentType());

        ShiftAssignmentResponseDTO response = toResponseDTO(savedAssignment);

        System.out.println("DTO CREATO: " + response);

        return response;
    }


    //2 MODIFICA UN'ASSEGNAZIONE(INVIO EMAIL ALLO USER)
    public ShiftAssignmentResponseDTO updateShiftAssignment(UUID id, UpdateShiftAssignmentDTO payload) {
        // 1. Recupero l'assegnazione esistente
        ShiftAssignment assignment = shiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assegnazione non trovata."));

        // 2. REGOLA: Controllo se il turno è già completato (passato)
        if (assignment.getShift() != null && assignment.getAssignmentType() == AssignmentType.WORK) {

            LocalDateTime shiftStartDateTime = LocalDateTime.of(
                    assignment.getShiftDate(),
                    assignment.getShift().getStartTime()
            );

            LocalDateTime shiftEndDateTime = LocalDateTime.of(
                    assignment.getShiftDate(),
                    assignment.getShift().getEndTime()
            );

            // Il turno è già terminato
            if (LocalDateTime.now().isAfter(shiftEndDateTime)) {
                throw new BadRequestException(
                        "Non è possibile modificare un'assegnazione il cui turno è già completato."
                );
            }

            // Il turno è oggi e mancano meno di 2 ore all'inizio
            if (assignment.getShiftDate().isEqual(LocalDate.now())
                    && LocalDateTime.now().isAfter(shiftStartDateTime.minusHours(2))) {
                throw new BadRequestException(
                        "L'assegnazione del turno è modificabile fino a 2 ore prima dell'inizio."
                );
            }

        } else {

            if (!assignment.getShiftDate().isAfter(LocalDate.now().minusDays(1))) {
                throw new BadRequestException(
                        "Non è possibile modificare un'assegnazione OFF per oggi o per una data passata."
                );
            }
        }

        //3. Aggiornamento parziale
        if (payload.userId() != null) {
            User user = userService.findById(payload.userId());
            assignment.setUser(user);
        }

        if (payload.assignmentType() != null) {
            assignment.setAssignmentType(payload.assignmentType());
        }

        // Gestione logica del turno in base al tipo
        AssignmentType currentType = payload.assignmentType() != null ? payload.assignmentType() : assignment.getAssignmentType();

        if (currentType == AssignmentType.WORK) {
            if (payload.shiftId() != null) {
                Shift shift = shiftService.findById(payload.shiftId());
                assignment.setShift(shift);
            } else if (assignment.getShift() == null) {
                throw new BadRequestException("Il turno è obbligatorio per i giorni di lavoro.");
            }
        } else {

            assignment.setShift(null);
        }

        //4 salvataggio

        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(assignment);

        //5 invio email

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        if ((assignment.getShiftDate().equals(LocalDate.now())
                || assignment.getShiftDate().equals(tomorrow))
                && assignment.getShift() != null) {

            String htmlBody = EmailTemplateBuilder.buildShiftUpdated(
                    assignment.getShift().getOffice().getName(),
                    assignment.getShift().getStartTime(),
                    assignment.getShift().getEndTime(),
                    assignment.getShiftDate(),
                    assignment.getUser().getName(),
                    userPageUrl
            );

            emailService.sendHtmlEmail(
                    assignment.getUser().getAccount().getEmail(),
                    "[Turno modificato - YouRoster!]",
                    htmlBody
            );
        }

        return toResponseDTO(updatedAssignment);
    }

    //3 CANCELLA UN'ASSEGNAZIONE
    public void deleteShiftAssignment(UUID id) {
        ShiftAssignment shiftAssignment = shiftAssignmentRepository.findById(id).orElseThrow(() -> new NotFoundException("L'assegnazione del turno non è stata trovata"));

        LocalDateTime shiftStartDateTime = LocalDateTime.of(
                shiftAssignment.getShiftDate(),
                shiftAssignment.getShift().getStartTime()
        );

        if (LocalDateTime.now().isAfter(shiftStartDateTime.minusHours(24))) {
            throw new BadRequestException(
                    "Non è possibile eliminare un'assegnazione nelle 24 ore precedenti l'inizio del turno."
            );
        }


        shiftAssignmentRepository.delete(shiftAssignment);
    }


    //4 VISUALIZZA LE ASSEGNAZIONI PER DATA
    public Page<ShiftAssignmentResponseDTO> getAssignmentsByDateAndFilters(LocalDate shiftDate, String officeName, AssignmentType assignmentType, Pageable pageable) {

        Page<ShiftAssignment> assignmentsPage = shiftAssignmentRepository.findByDateAndFilters(shiftDate, officeName, assignmentType, pageable);
        System.out.println("ASSEGNAZIONI TROVATE: " + assignmentsPage.getTotalElements());
        return assignmentsPage.map(this::toResponseDTO);
    }

    //5 VISUALIZZA LE ASSEGNAZIONI DA DATA X A DATA Y PER UFFICIO
    public Page<ShiftAssignmentResponseDTO> getAssignmentsBetweenDatesAndFilters(LocalDate startDate, LocalDate endDate, String officeName, AssignmentType assignmentType, Pageable pageable) {

        Page<ShiftAssignment> assignmentsPage = shiftAssignmentRepository.findByDateBetweenAndFilters(startDate, endDate, officeName, assignmentType, pageable);


        return assignmentsPage.map(this::toResponseDTO);
    }

    //6 VISUALIZZA LE PROPRIE ASSEGNAZIONI
    public Page<ShiftAssignmentResponseDTO> getMyAssignment(Account currentAccount, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        Page<ShiftAssignment> assignmentsPage;

        if (startDate != null && endDate != null) {
            // Se l'utente specifica un intervallo
            assignmentsPage = shiftAssignmentRepository.findByUserAndShiftDateBetween(currentAccount.getUser(), startDate, endDate, pageable);
        } else {
            // Se non mette le date, possiamo creare un metodo ad hoc nel repository o filtrare per utente e basta
            assignmentsPage = shiftAssignmentRepository.findByUser(currentAccount.getUser(), pageable);
        }

        assignmentsPage.getContent().forEach(assignment ->
                System.out.println(
                        "ASSIGNMENT ME -> DATA: " + assignment.getShiftDate()
                                + " | TIPO: " + assignment.getAssignmentType()
                                + " | USER: " + assignment.getUser().getName()
                )
        );

        return assignmentsPage.map(this::toResponseDTO);

    }

    //7 VISUALIZZA UTENTI IN TURNO CON TE
    public List<ShiftAssignmentResponseDTO> getColleaguesOnMyShift(LocalDate shiftDate, Account currentAccount) {

        //1 recupero l'utente
        User user = currentAccount.getUser();

        //2 cerco la sua assegnazione per quel giorno
        ShiftAssignment myShiftAssignment = shiftAssignmentRepository.findByUserAndShiftDate(user, shiftDate).orElseThrow(() -> new NotFoundException("Non hai nessuna assegnazione per questa data."));

        //3 se l'utente è in malattia, off, o ferie, quindi shift è null, non ha colleghi in turno
        if (myShiftAssignment.getShift() == null) {
            throw new BadRequestException("In questa data sei in " + myShiftAssignment.getAssignmentType() + ", non hai un turno di lavoro.");
        }

        //4 estraggo gli orati del turno e dell'ufficio del dipendente
        LocalTime myStartTime = myShiftAssignment.getShift().getStartTime();
        LocalTime myEndTime = myShiftAssignment.getShift().getEndTime();
        UUID myOfficeId = myShiftAssignment.getShift().getOffice().getId();
        List<String> myTasks = myShiftAssignment.getTasks().stream().map(shiftTask -> shiftTask.getTaskTitle().getTitle()).toList();

        //5 cerco gli altri dipendenti con il turno che si sovrappone a quello del dipendente
        List<ShiftAssignment> colleaguesAssignments = shiftAssignmentRepository
                .findOverlappingColleagues(shiftDate, user, myOfficeId, myStartTime, myEndTime);

        //6 return
        return colleaguesAssignments.stream()
                .map(this::toResponseDTO)
                .toList();

    }

    //8. COORDINATOR DEVE POTER VISUALIZZARE CHI E' IN TURNO IN QUEL GIORNO IN QUELLA SEDE
    public List<ShiftAssignmentResponseDTO> getDailyAssignmentsForCoordinator(LocalDate date, Account currentAccount) {

        // 1. Verifico l'ufficio del coordinatore loggato
        if (currentAccount.getUser().getReferenceOffice() == null) {
            throw new BadRequestException("Non sei associato a nessun ufficio.");
        }
        UUID officeId = currentAccount.getUser().getReferenceOffice().getId();

        // 2. Recupero tutte le assegnazioni valide per quella data e quell'ufficio
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByShiftDateAndOfficeId(date, officeId);

        // 3. return
        return assignments.stream()
                .map(this::toResponseDTO)
                .toList();

    }

}
