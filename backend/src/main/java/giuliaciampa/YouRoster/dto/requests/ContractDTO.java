package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.ContractType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ContractDTO(

        @NotNull(message = "Il dipendente è obbligatorio")
        UUID userId,

        @NotNull(message = "La data di inizio è obbligatoria")
        LocalDate startDate,

        LocalDate endDate,
        @NotNull(message = "Il tipo di contratto è obbligatorio")
        ContractType contractType,

        @Min(value = 0, message = "Le ore di permesso spettanti non possono essere negative")
        int hoursOfLeaveDuePerYear,

        @Min(value = 0, message = "I giorni di ferie spettanti non possono essere negativi")
        int daysOfLeaveDuePerYear,

        int workingHoursPerWeek
) {
}
