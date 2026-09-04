package giuliaciampa.YouRoster.controllers;

import giuliaciampa.YouRoster.dto.responses.UserSummaryResponseDTO;
import giuliaciampa.YouRoster.entities.Account;
import giuliaciampa.YouRoster.services.UserSummaryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaves/summary")
public class UserSummaryController {

    private final UserSummaryService leaveSummaryService;


    public UserSummaryController(UserSummaryService leaveSummaryService) {
        this.leaveSummaryService = leaveSummaryService;
    }

    @GetMapping
    public UserSummaryResponseDTO getLeaveSummary(@AuthenticationPrincipal Account currentAccount) {
        return leaveSummaryService.getUserLeaveSummary(currentAccount);

    }
}

