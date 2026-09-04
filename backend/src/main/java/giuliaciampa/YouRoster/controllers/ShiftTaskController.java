package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ShiftTaskRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftTaskUpdateRequestDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftTaskResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ShiftTaskService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shift-tasks")
public class ShiftTaskController {
    private final ShiftTaskService shiftTaskService;

    public ShiftTaskController(ShiftTaskService shiftTaskService) {
        this.shiftTaskService = shiftTaskService;
    }

    //1. CREA NUOVO TASK
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','COORDINATOR')")
    public ShiftTaskResponseDTO assignNewTask(@RequestBody @Validated ShiftTaskRequestDTO payload, BindingResult validationResult, @AuthenticationPrincipal Account currentAccount) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftTaskService.saveNewTask(payload, currentAccount);

    }

    //2. MODIFICA TASK
    @PatchMapping("/{shiftTaskId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','COORDINATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftTaskResponseDTO updateTask(@PathVariable UUID shiftTaskId, @RequestBody @Validated ShiftTaskUpdateRequestDTO payload, BindingResult validationResult, @AuthenticationPrincipal Account currentAccount) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftTaskService.updateTask(shiftTaskId, payload, currentAccount);

    }

    //3. ELIMINA TASK
    @DeleteMapping("/{shiftTaskId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','COORDINATOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable UUID shiftTaskId, @AuthenticationPrincipal Account currentAccount) {
        shiftTaskService.deleteTask(shiftTaskId, currentAccount);
    }

    //4. GET ALL PER UNO SPECIFICO TURNO
    @GetMapping("/{shiftAssignmentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','COORDINATOR')")
    public List<ShiftTaskResponseDTO> getTasksByShiftAssignment(@PathVariable UUID shiftAssignmentId, @AuthenticationPrincipal Account currentAccount) {
        return shiftTaskService.getTasksByShiftAssignment(shiftAssignmentId, currentAccount);
    }

    //5. GET TUTTI I TASK ASSEGNATI NEL GIORNO IN QUELL'UFFICIO
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','COORDINATOR')")
    public Page<ShiftTaskResponseDTO> getDailyTasksForCoordinatorOffice(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftAssignment.shiftDate") String sortBy,
            @RequestParam(required = false) LocalDate date) {


        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return shiftTaskService.getDailyTasksForCoordinatorOffice(date, pageable, currentAccount);
    }

    //6.VISUALIZZA I PROPRI TURNI
    @GetMapping("/my-tasks")
    public List<ShiftTaskResponseDTO> getMyTasks(@AuthenticationPrincipal Account currentAccount) {
        return shiftTaskService.getMyTasksForCurrentShift(currentAccount);
    }

}
