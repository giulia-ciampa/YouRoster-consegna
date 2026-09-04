package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;

public record RestShiftDTO(
        @NotNull(message = "Il campo isRest è obbligatorio")
        Boolean isRest
) {
}
