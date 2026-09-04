package giuliaciampa.YouRoster.dto.responses;

import giuliaciampa.YouRoster.entities.AccountStatus;

import java.util.Set;
import java.util.UUID;

public record AccountSummaryDTO(
        UUID accountId,
        String name,
        String surname,
        String phoneNumber,
        String photoUrl,
        String officeName,
        String email,
        AccountStatus status,
        Set<String> roleNames
) {
}
