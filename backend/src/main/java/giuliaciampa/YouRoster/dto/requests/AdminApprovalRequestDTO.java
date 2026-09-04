package giuliaciampa.YouRoster.dto.requests;

import java.util.Set;
import java.util.UUID;

public record AdminApprovalRequestDTO(

        Set<String> roles,

        UUID officeId //null se l'utente non è un coordinator o se non ha una sede assegnata

) {
}
