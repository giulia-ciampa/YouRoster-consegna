package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateAccountRoleDTO(
        @NotEmpty(message = "È necessario specificare almeno un ruolo.")
        Set<String> roles) {
}
