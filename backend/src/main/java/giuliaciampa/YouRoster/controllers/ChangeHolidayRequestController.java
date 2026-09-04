package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ChangeHolidayRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeHolidayRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeHolidayRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ChangeHolidayRequestService;
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
@RequestMapping("/change-holiday-requests")
public class ChangeHolidayRequestController {

    private final ChangeHolidayRequestService changeHolidayRequestService;

    public ChangeHolidayRequestController(ChangeHolidayRequestService changeHolidayRequestService) {
        this.changeHolidayRequestService = changeHolidayRequestService;
    }

    //1. NUOVA RICHIESTA DI MODIFICA
    @PostMapping("/{originalRequestId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeHolidayRequestResponseDTO saveChangeRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID originalRequestId,
            @RequestBody @Validated ChangeHolidayRequestDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return changeHolidayRequestService.saveChangeRequest(currentAccount, originalRequestId, payload);

    }

    //2. MODIFICA RICHIESTA DI MODIFICA IF STATUS SENT
    @PatchMapping("/{requestId}")
    public ChangeHolidayRequestResponseDTO updateRequestIfSent(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated ChangeHolidayRequestDTO payload,
            BindingResult validationResult,
            @PathVariable UUID requestId) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return changeHolidayRequestService.updateRequestIfNotSent(currentAccount, payload, requestId);
    }

    //3. ELIMINA RICHIESTA DI MODIFICA IF STATUS SENT
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void DeleteIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        changeHolidayRequestService.deleteIfStatusIsNotSent(currentAccount, requestId);
    }

    //4. GET MY REQUESTS
    @GetMapping("/me")
    public Page<ChangeHolidayRequestReviewerResponseDTO> getMyRequests(
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeHolidayRequestService.getMyRequests(currentAccount, startDate, endDate, status, pageable);

    }

    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    @GetMapping("/filtered")
    public Page<ChangeHolidayRequestReviewerResponseDTO> getAllWithFilters(
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

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeHolidayRequestService.getAllWithFilters(currentAccount, name, status, startDate, endDate, pageable);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    @GetMapping("/pending")
    public Page<ChangeHolidayRequestResponseDTO> getPendingRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy

    ) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeHolidayRequestService.getPendingRequests(currentAccount, name, startDate, endDate, pageable);
    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED"
    @PatchMapping("/{requestId}/cancel")
    public ChangeHolidayRequestReviewerResponseDTO cancelRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return changeHolidayRequestService.cancelRequest(currentAccount, requestId);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    @PatchMapping("{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeHolidayRequestReviewerResponseDTO approveRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {


        return changeHolidayRequestService.approveRequest(payload, currentAccount, requestId);
    }

    //9. ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeHolidayRequestReviewerResponseDTO rejectRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {


        return changeHolidayRequestService.rejectRequest(payload, currentAccount, requestId);
    }

    //GET SINGOLA RICHIESTA
    @GetMapping("/{originalRequestId}")
    public ChangeHolidayRequestResponseDTO getMyChangeRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID originalRequestId
    ) {
        return changeHolidayRequestService.getMyChangeRequest(
                currentAccount,
                originalRequestId
        );
    }

    //GET SINGOLA RICHIESTA PER ADMIN E SHIFT MANAGER
    @GetMapping("/{requestId}/reviewer")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeHolidayRequestResponseDTO getChangeRequestForReviewer(@PathVariable UUID requestId) {
        return changeHolidayRequestService.getChangeRequestForReviewer(requestId);

    }

}
