package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "L'email è un campo obbligatorio")
        @Email(message = "Inserisci un indirizzo email valido")
        String email,

        @NotBlank(message = "La password è un campo obbligatorio")
        String password
) {
}
