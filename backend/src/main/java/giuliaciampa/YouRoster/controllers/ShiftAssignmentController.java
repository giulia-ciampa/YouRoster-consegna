package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateShiftAssignmentDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftAssignmentResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ShiftAssignmentService;
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
@RequestMapping("/shift-assignments")
public class ShiftAssignmentController {

    private final ShiftAssignmentService shiftAssignmentService;

    public ShiftAssignmentController(ShiftAssignmentService shiftAssignmentService) {
        this.shiftAssignmentService = shiftAssignmentService;
    }

    //1. CREA UN'ASSEGNAZIONE
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public ShiftAssignmentResponseDTO saveNewAssignment(@RequestBody @Validated ShiftAssignmentDTO payload, BindingResult validationResult) {


        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftAssignmentService.saveNewAssignment(payload);
    }

    //2. MODIFICA UN'ASSEGNAZIONE(INVIO EMAIL ALLO USER)
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public ShiftAssignmentResponseDTO updateShiftAssignment(
            @PathVariable UUID id,
            @RequestBody @Validated UpdateShiftAssignmentDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftAssignmentService.updateShiftAssignment(id, payload);
    }

    //3. CANCELLA UN'ASSEGNAZIONE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShiftAssignment(@PathVariable UUID id) {

        shiftAssignmentService.deleteShiftAssignment(id);

    }


    //4. VISUALIZZA LE ASSEGNAZIONI PER DATA
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public Page<ShiftAssignmentResponseDTO> getAssignmentsByDateAndFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftDate") String sortBy,
            @RequestParam LocalDate shiftDate,
            @RequestParam(required = false) String officeName,
            @RequestParam(required = false) AssignmentType assignmentType
    ) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return shiftAssignmentService.getAssignmentsByDateAndFilters(shiftDate, officeName, assignmentType, pageable);
    }

    //5. VISUALIZZA LE ASSEGNAZIONI DA DATA X A DATA Y PER UFFICIO
    @GetMapping("/between-dates")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public Page<ShiftAssignmentResponseDTO> getAssignmentsBetweenDatesAndFilters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "shiftDate") String sortBy,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String officeName,
            @RequestParam(required = false) AssignmentType assignmentType) {

        if (size <= 0) size = 10;
        if (size > 1000) size = 1000;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        return shiftAssignmentService.getAssignmentsBetweenDatesAndFilters(startDate, endDate, officeName, assignmentType, pageable);
    }

    //6. VISUALIZZA LE PROPRIE ASSEGNAZIONI
    @GetMapping("/me")
    public Page<ShiftAssignmentResponseDTO> getMyAssignment(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftDate") String sortBy,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {


        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        if (size <= 0) size = 10;
        if (size > 1000) size = 1000;
        if (page < 0) page = 0;


        return shiftAssignmentService.getMyAssignment(currentAccount, startDate, endDate, pageable);
    }


    //7. VISUALIZZA UTENTI IN TURNO CON TE
    @GetMapping("/colleagues-onshift")
    public List<ShiftAssignmentResponseDTO> getColleaguesOnMyShift(@RequestParam LocalDate shiftDate, @AuthenticationPrincipal Account currentAccount) {
        return shiftAssignmentService.getColleaguesOnMyShift(shiftDate, currentAccount);
    }

    //8. COORDINATOR DEVE POTER VISUALIZZARE CHI E' IN TURNO IN QUEL GIORNO IN QUELLA SEDE
    public List<ShiftAssignmentResponseDTO> getDailyAssignmentsForCoordinator(@RequestParam LocalDate date, @AuthenticationPrincipal Account currentAccount) {
        return shiftAssignmentService.getDailyAssignmentsForCoordinator(date, currentAccount);
    }


}



