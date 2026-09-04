package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.requests.AbsenceCertificationRequestDTO;
import giuliaciampa.YouRoster.dto.requests.ReviewerNotesDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateCertificationRequestDTO;
import giuliaciampa.YouRoster.dto.responses.AbsenceCertificationResponseDTO;
import giuliaciampa.YouRoster.dto.responses.AbsenceCertificationReviewResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.entities.RequestStatus;
import giuliaciampa.YouRoster.exceptions.ValidationException;
import giuliaciampa.YouRoster.services.AbsenceCertificationRequestService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/certifications")
public class AbsenceCertificationRequestController {

    private final AbsenceCertificationRequestService absenceCertificationRequestService;

    public AbsenceCertificationRequestController(AbsenceCertificationRequestService absenceCertificationRequestService) {
        this.absenceCertificationRequestService = absenceCertificationRequestService;
    }

    //1. NUOVA RICHIESTA CERTIFICATA
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AbsenceCertificationResponseDTO newCertificationRequest(@AuthenticationPrincipal Account currentAccount, @ModelAttribute @Validated AbsenceCertificationRequestDTO payload, BindingResult validationResult) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return absenceCertificationRequestService.newCertificationRequest(currentAccount, payload);
    }


    //2. MODIFICA RICHIESTA SE LO STATO È ANCORA SENT
    @PatchMapping(value = "/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AbsenceCertificationResponseDTO updateRequest(
            @AuthenticationPrincipal Account currentAccount,
            @ModelAttribute @Validated UpdateCertificationRequestDTO payload,
            BindingResult validationResult,
            @PathVariable UUID requestId) {

        if (validationResult.hasErrors()) {
            List<String> errorsList = validationResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            throw new ValidationException(errorsList);
        }

        return absenceCertificationRequestService.updateRequest(currentAccount, payload, requestId);
    }

    //3. ELIMINA RICHIESTA SE LO STATO È ANCORA SENT
    @DeleteMapping("/{requestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRequestIfSentStatus(@AuthenticationPrincipal Account currentAccount, @PathVariable UUID requestId) {
        absenceCertificationRequestService.deleteRequestIfSentStatus(currentAccount, requestId);
    }

    //4. VISUALIZZA LE MIE RICHIESTE CERTIFICATE
    @GetMapping("/myRequests")
    public Page<AbsenceCertificationReviewResponseDTO> getMyCertifications(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) RequestStatus status) {


        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        return absenceCertificationRequestService.getMyCertifications(currentAccount, startDate, endDate, status, pageable);

    }


    //5. METODO GET RICHIESTE CERTIFICATE CON FILTRI OPZIONALI
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public Page<AbsenceCertificationReviewResponseDTO> getAllWithFilters(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @AuthenticationPrincipal Account currentAccount,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return absenceCertificationRequestService.getAllWithFilters(currentAccount, name, status, startDate, endDate, pageable);


    }

    //6. METODO GET RICHIESTE DA LAVORARE(STATO SENT)
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public Page<AbsenceCertificationResponseDTO> getPendingRequests(
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

        return absenceCertificationRequestService.getPendingRequests(currentAccount, name, startDate, endDate, pageable);
    }

    //7. ADMIN O HR APPROVANO LA RICHIESTA
    @PatchMapping("{requestId}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public AbsenceCertificationReviewResponseDTO approveRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ReviewerNotesDTO payload
    ) {

        return absenceCertificationRequestService.approveRequest(payload, currentAccount, requestId);
    }

    //8. ADMIN O HR RIFIUTANO LA RICHIESTA
    @PatchMapping("{requestId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN','HR')")
    public AbsenceCertificationReviewResponseDTO rejectRequest(
            @AuthenticationPrincipal Account currentAccount,
            @PathVariable UUID requestId,
            @RequestBody(required = false) ReviewerNotesDTO payload) {
        return absenceCertificationRequestService.rejectRequest(payload, currentAccount, requestId);
    }

    //9. RICHIESTE CERTIFICATE APPROVATE
    @GetMapping("/approved")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public Page<AbsenceCertificationReviewResponseDTO> getApprovedCertifications(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "startDate") String sortBy) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return absenceCertificationRequestService
                .getApprovedCertifications(startDate, endDate, pageable);
    }

}
