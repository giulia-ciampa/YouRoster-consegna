package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ShiftTaskTitleRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftTaskTitleUpdateRequestDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftTaskTitleResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ShiftTaskTitleService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shift-task-titles")
public class ShiftTaskTitleController {

    private final ShiftTaskTitleService shiftTaskTitleService;

    public ShiftTaskTitleController(ShiftTaskTitleService shiftTaskTitleService) {
        this.shiftTaskTitleService = shiftTaskTitleService;
    }

    //1. CREA NUOVO TITOLO(COORDINATOR, ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public ShiftTaskTitleResponseDTO saveNewTaskTitle(
            @RequestBody @Validated ShiftTaskTitleRequestDTO payload,
            BindingResult validationResult,
            @AuthenticationPrincipal Account currentAccount) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftTaskTitleService.saveNewTaskTitle(payload, currentAccount);
    }

    //2. MODIFICA TITOLO (COORDINATOR, ADMIN)
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public ShiftTaskTitleResponseDTO updateTaskTitle(
            @PathVariable UUID id,
            @RequestBody @Validated ShiftTaskTitleUpdateRequestDTO payload,
            BindingResult validationResult,
            @AuthenticationPrincipal Account currentAccount) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftTaskTitleService.updateTaskTitle(id, payload, currentAccount);
    }


    //3. DISATTIVA TITOLO (COORDINATOR, ADMIN)
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public ShiftTaskTitleResponseDTO deactivateTaskTitle(@PathVariable UUID id, @AuthenticationPrincipal Account currentAccount) {
        return shiftTaskTitleService.deactivateTaskTitle(id, currentAccount);
    }

    //4. RIATTIVA TITOLO (COORDINATOR, ADMIN)
    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public ShiftTaskTitleResponseDTO reactivateTaskTitle(@PathVariable UUID id, @AuthenticationPrincipal Account currentAccount) {
        return shiftTaskTitleService.reactivateTaskTitle(id, currentAccount);
    }

    //5. GET ALL PER UFFICIO (ATTIVI)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public List<ShiftTaskTitleResponseDTO> GetAllActiveTitleByOffice(@AuthenticationPrincipal Account currentAccount) {
        return shiftTaskTitleService.GetAllActiveTitleByOffice(currentAccount);
    }

    //6. PERMETTERE LA RICERCA DIGITANDO IL TITOLO
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COORDINATOR')")
    public List<ShiftTaskTitleResponseDTO> searchTitles(
            @RequestParam String name,
            @AuthenticationPrincipal Account currentAccount) {

        return shiftTaskTitleService.searchActiveTitles(name, currentAccount);
    }


}
