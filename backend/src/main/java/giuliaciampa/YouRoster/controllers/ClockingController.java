package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ClockingDTO;
import giuliaciampa.YouRoster.dto.requests.ManualClockingAdminDTO;
import giuliaciampa.YouRoster.dto.responses.ClockingResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.AttendanceStatus;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ClockingService;
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
@RequestMapping("/clockings")
public class ClockingController {

    private final ClockingService clockingService;

    public ClockingController(ClockingService clockingService) {
        this.clockingService = clockingService;
    }

    //1. METODO CHECK-IN
    @PostMapping("/in")
    @ResponseStatus(HttpStatus.CREATED)
    public ClockingResponseDTO clockIn(@RequestBody @Validated ClockingDTO payload, BindingResult validationResult, @AuthenticationPrincipal Account currentAccount) {


        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }


        return clockingService.createClockIn(payload, currentAccount);
    }

    //2. METODO CHECK-OUT
    @PatchMapping("/out")
    public ClockingResponseDTO clockOut(@RequestBody @Validated ClockingDTO payload, BindingResult validationResult, @AuthenticationPrincipal Account currentAccount) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return clockingService.createClockOut(payload, currentAccount);
    }

    //3. METODO GET TIMBRATURE
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<ClockingResponseDTO> getClockings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftAssignment.shiftDate") String sortBy,
            @RequestParam(required = false) String officeName,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status
    ) {
        // Se l'admin ha inserito una data singola, facciamo coincidere inizio e fine
        if (date != null) {
            startDate = date;
            endDate = date;
        }

        if (date == null) {
            throw new BadRequestException("Inserire almeno una data");
        }

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());


        return clockingService.getFilteredClockings(officeName, startDate, endDate, status, pageable);
    }

    //4. METODO GET MIE TIMBRATURE
    @GetMapping("/me")
    public Page<ClockingResponseDTO> getMyClockings(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftAssignment.shiftDate") String sortBy,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        if (date != null) {
            startDate = date;
            endDate = date;
        }

        if (date == null) {
            throw new BadRequestException("Inserire almeno una data");
        }

        return clockingService.getMyClockings(currentAccount, startDate, endDate, pageable);

    }

    //5. METODO GET TIMBRATURE DI UNO SPECIFICO USER
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'AP_AND_PAYROLL_SPECIALIST')")
    public Page<ClockingResponseDTO> getClockingsBySearch(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "shiftAssignment.shiftDate") String sortBy,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        if (date != null) {
            startDate = date;
            endDate = date;
        }

        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        return clockingService.getClockingsBySearch(name, startDate, endDate, pageable);
    }

    //6. MODIFICA O CORREZIONE MANUALE
    @PatchMapping("/admin/override")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ClockingResponseDTO manualOverrideClocking(@RequestBody @Validated ManualClockingAdminDTO payload, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return clockingService.manualOverrideClocking(payload);
    }

    //7 GET CLOCKING BY USER
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'AP_AND_PAYROLL_SPECIALIST')")
    public Page<ClockingResponseDTO> getClockingsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) LocalDate date
    ) {
        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return clockingService.getClockingsByUser(userId, date, pageable);
    }

    //8. GET TIMBRATURA SINGOLO GIORNO
    @GetMapping("/me/date")
    public ClockingResponseDTO getMyClockingForDate(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam LocalDate date
    ) {

        return clockingService.getMyClockingForDate(currentAccount, date);
    }


}
