package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.responses.ChangeRequestResponseDTO;
import giuliaciampa.YouRoster.services.RequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SHIFT MANAGER')")
    public Page<ChangeRequestResponseDTO> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).descending()
        );

        return requestService.getRequestsForShiftManager(pageable);
    }


    // GET TUTTE LE RICHIESTE - ADMIN E SHIFT MANAGER
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN','SHIFT MANAGER')")
    public Page<ChangeRequestResponseDTO> getAllRequests(Pageable pageable) {
        return requestService.getAllRequests(pageable);
    }
}



