package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.Size;

public record ShiftTaskTitleUpdateRequestDTO(
        @Size(min = 3, max = 100, message = "Il titolo deve essere tra 3 e 100 caratteri")
        String title,
        String description
) {
}
