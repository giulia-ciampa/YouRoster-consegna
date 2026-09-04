package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.OfficeDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateOfficeDTO;
import giuliaciampa.YouRoster.dto.responses.OfficeResponseDTO;
import giuliaciampa.YouRoster.entities.OfficeStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.OfficeService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/offices")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    //1. CREA NUOVO UFFICIO/SEDE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public OfficeResponseDTO saveNewOffice(@RequestBody @Validated OfficeDTO payload, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }
        return officeService.saveNewOffice(payload);
    }

    //2. AGGIORNA UFFICIO/SEDE
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public OfficeResponseDTO updateOffice(@PathVariable UUID id, @RequestBody @Validated UpdateOfficeDTO payload, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }
        return officeService.updateOffice(id, payload);
    }

    //3. GET ALL OFFICES BY STATUS
    @GetMapping
    public List<OfficeResponseDTO> getOfficesByStatus(@RequestParam(required = false) OfficeStatus status) {
        return officeService.getAllOfficesByStatus(status);

    }

    //4. GET ACTIVE OFFICES
    @GetMapping("/active")
    public List<OfficeResponseDTO> getActiveOffice() {
        return officeService.getActiveOffice();
    }

    //5. GET OFFICE BY NAME
    @GetMapping("/management/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'MANAGER', 'AP E PAYROLL SPECIALIST')")
    public OfficeResponseDTO getOfficeByNameAdmin(@RequestParam String name) {
        return officeService.getOfficeByName(name);
    }


    //6. GET ACTIVE OFFICE BY NAME
    @GetMapping("/search")
    public OfficeResponseDTO getActiveOfficeByName(@RequestParam String name) {
        return officeService.getActiveOfficeByName(name);
    }

}
