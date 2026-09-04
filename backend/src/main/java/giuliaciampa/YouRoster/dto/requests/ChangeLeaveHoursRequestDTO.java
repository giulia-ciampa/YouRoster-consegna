package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.LeaveHoursType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ChangeLeaveHoursRequestDTO(
        @NotNull(message = "La data è obbligatoria")
        LocalDate date,

        @NotNull(message = "L'orario di inizio è obbligatorio")
        LocalTime startTime,

        @NotNull(message = "L'orario di fine è obbligatorio")
        LocalTime endTime,


        LeaveHoursType leaveHoursType,

        String employeeNotes
) {
}
