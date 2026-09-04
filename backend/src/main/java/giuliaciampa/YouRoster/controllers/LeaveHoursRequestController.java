package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.LeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateLeaveHoursRequestDTO;
import giuliaciampa.YouRoster.dto.responses.LeaveHoursRequestResponseDTO;
import giuliaciampa.YouRoster.dto.responses.LeaveHoursRequestReviewerResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.LeaveHoursRequestService;
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
@RequestMapping("/leave-hours")
public class LeaveHoursRequestController {

    private final LeaveHoursRequestService leaveHoursRequestService;

    public LeaveHoursRequestController(LeaveHoursRequestService leaveHoursRequestService) {
        this.leaveHoursRequestService = leaveHoursRequestService;
    }

    //1 CREA NUOVA RICHIESTA
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveHoursRequestResponseDTO saveNewRequest(
            @AuthenticationPrincipal Account currentAccount,
            @RequestBody @Validated LeaveHoursRequestDTO payload,
            BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return leaveHoursRequestService.saveNewRequest(currentAccount, payload);
    }

    //2. SE LA RICHIESTA E' SENT, CONSENTI MODIFICA
    @PatchMapping("/{requestId}")
    public LeaveHoursRequestResponseDTO updateIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            UpdateLeaveHoursRequestDTO payload,
            @PathVariable UUID requestId) {


        return leaveHoursRequestService.updateIfStatusIsSent(currentAccount, payload, requestId);
    }

    //3. SE LA RICHIESTA E' SENT, CONSENTI ELIMINAZIONE
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIfStatusIsSent(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {
        leaveHoursRequestService.deleteIfStatusIsSent(currentAccount, requestId);
    }

    //4. GET MY REQUESTS
    @GetMapping("/me")
    Page<LeaveHoursRequestReviewerResponseDTO> getMyRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy

    ) {
        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return leaveHoursRequestService.getMyRequests(currentAccount, date, startTime, endTime, status, pageable);
    }

    //5. GET ALL REQUEST CON FILTRI OPZIONALI(name, status, startDate, endDate)
    @GetMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<LeaveHoursRequestReviewerResponseDTO> getAllWithFilters(
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

        return leaveHoursRequestService.getAllWithFilters(currentAccount, name, status, date, startTime, endTime, pageable);
    }

    //6. GET REQUEST DA LAVORARE (SENT)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<LeaveHoursRequestResponseDTO> getPendingRequests(
            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
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

        return leaveHoursRequestService.getPendingRequests(currentAccount, name, date, startTime, endTime, pageable);
    }

    //7. SE NON E' SENT MA NON TI SERVE PIù, LO STATO DIVENTA "CANCELLED"
    @PatchMapping("/{requestId}/cancel")
    public LeaveHoursRequestReviewerResponseDTO cancelRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return leaveHoursRequestService.cancelRequest(currentAccount, requestId);
    }

    //8. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    @PatchMapping("{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public LeaveHoursRequestReviewerResponseDTO approveRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {


        return leaveHoursRequestService.approveRequest(payload, currentAccount, requestId);
    }

    //9. ADMIN - SHIFT MANAGER APPROVANO RICHIESTA
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public LeaveHoursRequestReviewerResponseDTO rejectRequest(
            @RequestBody(required = false) ReviewerNotesDTO payload,
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId) {

        return leaveHoursRequestService.rejectRequest(payload, currentAccount, requestId);
    }
}
