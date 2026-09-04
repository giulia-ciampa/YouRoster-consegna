package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.LeaveHoursType;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateLeaveHoursRequestDTO(

        LocalDate date,


        LocalTime startTime,


        LocalTime endTime,
        LeaveHoursType leaveHoursType,
        String employeeNotes
) {
}
