package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.ChangeLeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeLeaveHoursRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.ChangeLeaveHoursRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.ChangeLeaveHoursRequestService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/change-leave-hours-requests")
public class ChangeLeaveHoursRequestController {
    private final ChangeLeaveHoursRequestService changeLeaveHoursRequestService;

    public ChangeLeaveHoursRequestController(ChangeLeaveHoursRequestService changeLeaveHoursRequestService) {
        this.changeLeaveHoursRequestService = changeLeaveHoursRequestService;
    }

    //1. NUOVA RICHIESTA DI MODIFICA
    @PostMapping("/{originalRequestId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeLeaveHoursRequestResponseDTO SaveChangeRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID originalRequestId,
            @RequestBody @Validated ChangeLeaveHoursRequestDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return changeLeaveHoursRequestService.SaveChangeRequest(currentAccount, originalRequestId, payload);

    }

    //2. MODIFICA RICHIESTA DI MODIFICA IF STATUS SENT
    @PatchMapping("/{requestId}")
    public ChangeLeaveHoursRequestResponseDTO updateRequestIfSent(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated ChangeLeaveHoursRequestDTO payload,
            BindingResult validationResult,
            @PathVariable UUID requestId) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return changeLeaveHoursRequestService.updateRequestIfSent(currentAccount, payload, requestId);

    }

    //3. ELIMINA RICHIESTA DI MODIFICA IF STATUS SENT
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void DeleteIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        changeLeaveHoursRequestService.DeleteIfStatusIsSent(currentAccount, requestId);
    }


    //4. GET MY REQUESTS
    @GetMapping("/me")
    public Page<ChangeLeaveHoursRequestReviewerResponseDTO> getMyRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false) RequestStatus status,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeLeaveHoursRequestService.getMyRequests(currentAccount, date, startTime, endTime, status, pageable);
    }

    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    @GetMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<ChangeLeaveHoursRequestReviewerResponseDTO> getAllWithFilters(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeLeaveHoursRequestService.getAllWithFilters(currentAccount, name,
                status, date, startTime, endTime, pageable);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<ChangeLeaveHoursRequestResponseDTO> getPendingRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return changeLeaveHoursRequestService.getPendingRequests(currentAccount, name, date, startTime, endTime, pageable);

    }

    //7. SE SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED"
    @PatchMapping("/{requestId}/cancel")
    public ChangeLeaveHoursRequestReviewerResponseDTO cancelRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return changeLeaveHoursRequestService.cancelRequest(currentAccount, requestId);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    @PatchMapping("{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeLeaveHoursRequestReviewerResponseDTO approveRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return changeLeaveHoursRequestService.approveRequest(payload, currentAccount, requestId);
    }

    //9. ADMIN - SHIFT MANAGER RIFIUTANO RICHIESTA
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeLeaveHoursRequestReviewerResponseDTO rejectRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return changeLeaveHoursRequestService.rejectRequest(payload, currentAccount, requestId);
    }


    // GET SINGOLA RICHIESTA DI MODIFICA PERMESSO ORE
    @GetMapping("/{originalRequestId}")
    public ChangeLeaveHoursRequestResponseDTO getMyChangeRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID originalRequestId
    ) {
        return changeLeaveHoursRequestService.getMyChangeRequest(
                currentAccount,
                originalRequestId
        );
    }

    //GET SINGOLA RICHIESTA PER SHIFT MANAGER E ADMIN
    @GetMapping("/{requestId}/reviewer")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public ChangeLeaveHoursRequestResponseDTO getChangeRequestForReviewer(@PathVariable UUID requestId) {
        return changeLeaveHoursRequestService.getChangeRequestForReviewer(requestId);
    }


}
