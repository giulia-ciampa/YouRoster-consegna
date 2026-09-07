package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ShiftCreateDTO;
import giuliaciampa.YouRoster.dto.requests.ShiftUpdateDTO;
import giuliaciampa.YouRoster.dto.responses.ShiftResponseDTO;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ShiftService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shifts")
public class ShiftController {
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    //1. CREA NUOVI TURNI(ADMIN, SHIFT MANAGER)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponseDTO saveNewShift(@RequestBody @Validated ShiftCreateDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftService.saveNewShift(payload);
    }

    //2. MODIFICA TURNO(ADMIN, SHIFT MANAGER)
    @PatchMapping("/{shiftId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public ShiftResponseDTO updateShift(@PathVariable UUID shiftId, @RequestBody @Validated ShiftUpdateDTO payload, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return shiftService.updateShift(shiftId, payload);
    }


    //3. DISATTIVA TURNO (Soft Delete - ADMIN, SHIFT MANAGER)
    @PatchMapping("/{shiftId}/deactivate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public ShiftResponseDTO deactivateShift(@PathVariable UUID shiftId) {
        return shiftService.deactivateShift(shiftId);
    }

    //4. TROVA TURNI (TUTTI O FILTRATI PER STATO)
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public Page<ShiftResponseDTO> findShifts(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "40") int size,
                                             @RequestParam(defaultValue = "office.name") String sortBy,
                                             @RequestParam(required = false) Boolean isActive) {


        if (size <= 0) size = 10;
        if (size > 40) size = 40;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return shiftService.findShifts(isActive, pageable);
    }


    //5. RIATTIVAZIONE TURNO DISATTIVATO
    @PatchMapping("/{shiftId}/reactivate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public ShiftResponseDTO activateShift(@PathVariable UUID shiftId) {
        return shiftService.reactivateShift(shiftId);
    }


}
