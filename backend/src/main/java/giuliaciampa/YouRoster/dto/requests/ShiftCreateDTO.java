package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ShiftCreateDTO(
        @NotNull(message = "L'ufficio è obbligatorio")
        String officeName,
        @NotNull(message = "L'orario di inizio turno è obbligatorio")
        LocalTime startTime,
        @NotNull(message = "L'orario di fine turno è obbligatorio")
        LocalTime endTime


) {
}
