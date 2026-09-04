package giuliaciampa.YouRoster.dto.requests;

import java.time.LocalDate;

public record UpdateHolidayRequestDTO(

        LocalDate startDate,


        LocalDate endDate,

        String employeeNotes) {
}
