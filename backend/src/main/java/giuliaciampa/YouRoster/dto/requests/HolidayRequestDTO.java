package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayRequestDTO(
        @NotNull(message = "La data di inizio ferie è obbligatoria")
        LocalDate startDate,

        @NotNull(message = "La data di fine ferie è obbligatoria")
        LocalDate endDate,
        String employeeNotes
) {
}
