package giuliaciampa.YouRoster.dto.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ChangeHolidayRequestDTO(

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDate startDate,

        @NotNull(message = "La data di fine è obbligatoria")
        LocalDate endDate,

        @Size(max = 200, message = "Le note non possono superare i 200 caratteri")
        String employeeNotes
) {
}
