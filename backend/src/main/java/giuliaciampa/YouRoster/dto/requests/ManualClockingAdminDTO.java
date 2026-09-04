package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record ManualClockingAdminDTO(

        @NotNull(message = "indica a quale turno è associata la timbratura")
        UUID shiftAssignmentId,


        LocalTime checkInTime,

        LocalTime checkOutTime,


        AttendanceStatus attendanceStatus,

        String notesFromAdmin
) {
}
