package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.ShiftTaskRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftTaskUpdateRequestDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftTaskResponseDTO;
import giuliaciampa.YouRoster.emailTemplates.EmailTemplateBuilder;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.exceptions.UnauthorizedException;
import giuliaciampa.YouRoster.repositories.ShiftTaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ShiftTaskService {
    private final ShiftTaskRepository shiftTaskRepository;
    private final ShiftAssignmentService shiftAssignmentService;
    private final ShiftTaskTitleService shiftTaskTitleService;
    private final EmailService emailService;

    @Value("${addedTask.url}")
    private String taskPageUrl;


    public ShiftTaskService(ShiftTaskRepository shiftTaskRepository, ShiftAssignmentService shiftAssignmentService, ShiftTaskTitleService shiftTaskTitleService, EmailService emailService) {
        this.shiftTaskRepository = shiftTaskRepository;
        this.shiftAssignmentService = shiftAssignmentService;
        this.shiftTaskTitleService = shiftTaskTitleService;
        this.emailService = emailService;
    }

    //HELPER RISPOSTA
    private ShiftTaskResponseDTO mapToResponse(ShiftTask task) {
        return new ShiftTaskResponseDTO(
                task.getId(),
                task.getShiftAssignment().getId(),
                task.getShiftAssignment().getShiftDate(),
                task.getShiftAssignment().getAssignmentType().name(),
                task.getTime(),
                task.getTaskTitle().getId(),
                task.getTaskTitle().getTitle()
        );
    }

    //HELPER CONTROLLO UFFIIO
    private void validateUserOfficeAccess(Account currentAccount, Office taskOffice) {
        boolean isAdmin = currentAccount.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ADMIN"));

        if (isAdmin) return;

        Office userOffice = currentAccount.getUser().getReferenceOffice();

        if (userOffice == null || taskOffice == null || !userOffice.getId().equals(taskOffice.getId())) {
            throw new UnauthorizedException("Non hai i permessi per gestire mansioni in questo ufficio.");
        }
    }

    //1. CREA NUOVO TASK
    public ShiftTaskResponseDTO saveNewTask(ShiftTaskRequestDTO payload, Account currentAccount) {
        // 1. Recupero l'assegnazione del turno
        ShiftAssignment shiftAssignment = shiftAssignmentService.findById(payload.shiftAssignment());


        // 2. Controllo sull'ufficio del coordinatore
        validateUserOfficeAccess(currentAccount, shiftAssignment.getShift().getOffice());

        // 3. NUOVO CONTROLLO: La mansione deve essere compresa nell'orario del turno
        LocalTime taskTime = payload.time();

        Shift shift = shiftAssignment.getShift();

        if (taskTime.isBefore(shift.getStartTime()) || taskTime.isAfter(shift.getEndTime())) {
            throw new BadRequestException(
                    "L'orario della mansione (" + taskTime + ") deve essere compreso tra l'inizio (" +
                            shift.getStartTime() + ") e la fine (" + shift.getEndTime() + ") del turno."
            );
        }

        // 3. Controllo duplicati prima di creare l'oggetto
        boolean alreadyExists = shiftTaskRepository.existsByShiftAssignmentIdAndTaskTitleId(
                payload.shiftAssignment(),
                payload.taskTitle()
        );

        if (alreadyExists) {
            throw new BadRequestException("Questo dipendente ha già questa mansione assegnata per questo turno.");
        }

        // 4. Recupero il titolo della mansione
        ShiftTaskTitle taskTitle = shiftTaskTitleService.findById(payload.taskTitle());

        // 5. Creo e salvo il Task operativo
        ShiftTask task = new ShiftTask();
        task.setShiftAssignment(shiftAssignment);
        task.setTime(payload.time());
        task.setTaskTitle(taskTitle);

        ShiftTask savedTask = shiftTaskRepository.save(task);

        //invio email
        String htmlBody = EmailTemplateBuilder.buildNewTask(savedTask.getTaskTitle().getOffice().getName(), savedTask.getShiftAssignment().getShift().getStartTime(), savedTask.getShiftAssignment().getShift().getEndTime(), savedTask.getShiftAssignment().getShiftDate(), savedTask.getShiftAssignment().getUser().getName(), savedTask.getTaskTitle().getTitle(), taskPageUrl);
        emailService.sendHtmlEmail(savedTask.getShiftAssignment().getUser().getAccount().getEmail(), "[Mansione aggiunta - YouRoster!]", htmlBody);
        //return
        return mapToResponse(savedTask);
    }

    //2. MODIFICA TASK
    public ShiftTaskResponseDTO updateTask(UUID shiftTaskId, ShiftTaskUpdateRequestDTO payload, Account currentAccount) {
        //1. cerco il task
        ShiftTask foundShiftTask = shiftTaskRepository.findById(shiftTaskId).orElseThrow(() -> new NotFoundException("La mansione non è stata trovata"));

        //2. controllo sull'ufficio
        Office currentOffice = foundShiftTask.getShiftAssignment().getShift().getOffice();
        validateUserOfficeAccess(currentAccount, currentOffice);

        //3. aggiorno
        if (payload.time() != null && payload.time().isAfter(LocalTime.now())) {
            foundShiftTask.setTime(payload.time());
        }

        if (payload.taskTitle() != null) {
            ShiftTaskTitle newTaskTitle = shiftTaskTitleService.findById(payload.taskTitle());
            foundShiftTask.setTaskTitle(newTaskTitle);
        }

        //4. salvo
        ShiftTask updatedTask = shiftTaskRepository.save(foundShiftTask);

        //5. return
        return mapToResponse(updatedTask);
    }

    //3. ELIMINA TASK
    public void deleteTask(UUID shiftTaskId, Account currentAccount) {
        ShiftTask task = shiftTaskRepository.findById(shiftTaskId)
                .orElseThrow(() -> new NotFoundException("Mansione non trovata."));

        // Controllo ufficio
        Office office = task.getShiftAssignment().getShift().getOffice();
        validateUserOfficeAccess(currentAccount, office);

        //controllo orario
        if (task.getTime().isBefore(LocalTime.now())) {
            throw new BadRequestException("Impossibile eliminare la mansione dopo che è stata eseguita");
        }

        shiftTaskRepository.delete(task);
    }


    //4. GET ALL PER UNO SPECIFICO TURNO
    public List<ShiftTaskResponseDTO> getTasksByShiftAssignment(UUID shiftAssignmentId, Account currentAccount) {
        ShiftAssignment assignment = shiftAssignmentService.findById(shiftAssignmentId);

        // Controllo sicurezza ufficio
        validateUserOfficeAccess(currentAccount, assignment.getShift().getOffice());

        List<ShiftTask> tasks = shiftTaskRepository.findByShiftAssignmentId(shiftAssignmentId);

        return tasks.stream().map(this::mapToResponse).toList();
    }


    //5. GET TUTTI I TASK ASSEGNATI NEL GIORNO IN QUELL'UFFICIO
    public Page<ShiftTaskResponseDTO> getDailyTasksForCoordinatorOffice(LocalDate date, Pageable pageable, Account currentAccount) {
        Page<ShiftTask> tasks;

        // Se l'utente è ADMIN (o ha il ruolo admin), vede tutti i task senza filtro di ufficio
        if (currentAccount.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            // Se date è null, puoi usare un metodo che prende tutti o filtra solo per data
            tasks = (date != null)
                    ? shiftTaskRepository.findByShiftAssignment_ShiftDate(date, pageable)
                    : shiftTaskRepository.findAll(pageable);
        } else {
            // Altrimenti è un Coordinator e applichiamo il filtro per il suo ufficio
            if (currentAccount.getUser() == null || currentAccount.getUser().getReferenceOffice() == null) {
                throw new BadRequestException("Non sei associato a nessun ufficio.");
            }
            UUID officeId = currentAccount.getUser().getReferenceOffice().getId();
            tasks = shiftTaskRepository.findByDateAndOffice(date, officeId, pageable);
        }

        return tasks.map(this::mapToResponse);
    }

    //6.VISUALIZZA LE PROPRIE TASK
    public List<ShiftTaskResponseDTO> getMyTasksForCurrentShift(Account currentAccount) {
        // 1. Trova l'utente collegato all'account
        User user = currentAccount.getUser();

        // 2. Trova l'assegnazione attiva/odierna di questo utente (user_id)
        ShiftAssignment assignment = shiftAssignmentService.findByUserIdAndShiftDate(user.getId(), LocalDate.now());

        // 3. Recupera i task di quell'assegnazione
        List<ShiftTask> tasks = shiftTaskRepository.findByShiftAssignmentId(assignment.getId());


        return tasks.stream().map(this::mapToResponse).toList();
    }

}
