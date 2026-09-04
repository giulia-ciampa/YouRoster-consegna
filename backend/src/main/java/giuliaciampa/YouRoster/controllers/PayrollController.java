package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.responses.PayrollSummaryDTO;
import giuliaciampa.YouRoster.services.PayrollService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payroll")
@PreAuthorize("hasAnyAuthority('ADMIN', 'AP E PAYROLL SPECIALIST')")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // 1. GET - Riepilogo ferie e permessi di un singolo dipendente tramite ID utente
    @GetMapping("/summary/{userId}")
    public PayrollSummaryDTO getUserLeaveSummary(@PathVariable UUID userId) {
        return payrollService.getUserLeaveSummary(userId);
    }

    //2. find All
    @GetMapping("/summary")
    public Page<PayrollSummaryDTO> getAllUsersLeaveSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "startDate") String sortBy
    ) {

        if (size <= 0) size = 10;
        if (size > 15) size = 15;
        if (page < 0) page = 0;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());

        return payrollService.getAllUsersLeaveSummary(pageable);
    }

}
