package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.LeaveHoursType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record LeaveHoursRequestDTO(
        @NotNull(message = "Seleziona una data")
        @FutureOrPresent(message = "La data selezionata non può essere nel passato")
        LocalDate date,

        @NotNull(message = "Seleziona l'orario di inizio")
        LocalTime startTime,

        @NotNull(message = "Seleziona l'orario di fine")
        LocalTime endTime,

        @NotNull(message = "Seleziona il tipo di permesso")
        LeaveHoursType leaveHoursType,
        String employeeNotes
) {
}
