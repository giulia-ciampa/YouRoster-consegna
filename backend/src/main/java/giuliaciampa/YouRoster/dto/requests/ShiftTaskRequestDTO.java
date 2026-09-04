package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftTaskRequestDTO(
        @NotNull(message = "la mansione deve avere sempre un turno di riferimento.")
        UUID shiftAssignment,
        LocalTime time,
        @NotNull(message = "la mansione deve avere un titolo")
        UUID taskTitle
) {
}
