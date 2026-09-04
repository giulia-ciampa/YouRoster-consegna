package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.HolidayRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateHolidayRequestDTO;
import giuliaciampa.YouRoster.dto.responses.HolidayRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.HolidayRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.HolidayRequestService;
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
@RequestMapping("/holidays")
public class HolidayRequestController {
    private final HolidayRequestService holidayRequestService;

    public HolidayRequestController(HolidayRequestService holidayRequestService) {
        this.holidayRequestService = holidayRequestService;
    }

    //1. CREA NUOVA RICHIESTA
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HolidayRequestResponseDTO saveHolidayRequest(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated HolidayRequestDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return holidayRequestService.saveHolidayRequest(currentAccount, payload);

    }

    //2. SE LA RICHIESTA E' SENT, CONSENTI MODIFICA
    @PatchMapping("/{requestId}")
    public HolidayRequestResponseDTO updateIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody UpdateHolidayRequestDTO payload,
            @PathVariable UUID requestId) {


        return holidayRequestService.updateIfStatusIsSent(currentAccount, payload, requestId);
    }

    //3. SE LA RICHIESTA E' SENT, CONSENTI ELIMINAZIONE
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void DeleteIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        holidayRequestService.DeleteIfStatusIsSent(currentAccount, requestId);
    }

    //4. GET MY REQUESTS
    @GetMapping("/me")
    public Page<HolidayRequestReviewerResponseDTO> getMyRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) RequestStatus status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return holidayRequestService.getMyRequests(currentAccount, startDate, endDate, status, pageable);
    }


    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    @GetMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<HolidayRequestReviewerResponseDTO> getAllWithFilters(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,


            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return holidayRequestService.getAllWithFilters(currentAccount, name, status, startDate, endDate, pageable);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<HolidayRequestResponseDTO> getPendingRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return holidayRequestService.getPendingRequests(currentAccount, name, startDate, endDate, pageable);


    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED"
    @PatchMapping("/{requestId}/cancel")
    public HolidayRequestReviewerResponseDTO cancelRequest(@AuthenticationPrincipal Account currentAccount, @PathVariable UUID requestId) {
        return holidayRequestService.cancelRequest(currentAccount, requestId);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    @PatchMapping("{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public HolidayRequestReviewerResponseDTO approveRequest(@RequestBody(required = false) ReviewerNotesDTO payload,
                                                            @AuthenticationPrincipal Account currentAccount,
                                                            @PathVariable UUID requestId) {
        return holidayRequestService.approveRequest(payload, currentAccount, requestId);
    }

    //9 ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public HolidayRequestReviewerResponseDTO rejectRequest(@RequestBody(required = false) ReviewerNotesDTO payload,
                                                           @AuthenticationPrincipal Account currentAccount,
                                                           @PathVariable UUID requestId) {
        return holidayRequestService.rejectRequest(payload, currentAccount, requestId);
    }
}
