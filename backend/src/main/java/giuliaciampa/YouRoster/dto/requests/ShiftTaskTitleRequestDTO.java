package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ShiftTaskTitleRequestDTO(
        @NotBlank(message = "Il titolo del task è obbligatorio")
        @Size(min = 3, max = 100, message = "Il titolo deve essere tra 3 e 100 caratteri")
        String title,
        String description,

        @NotNull(message = "L'ufficio è obbligatorio")
        UUID officeId

) {
}
