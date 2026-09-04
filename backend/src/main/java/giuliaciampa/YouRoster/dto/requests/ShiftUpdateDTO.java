package giuliaciampa.YouRoster.dto.requests;

import java.time.LocalTime;

public record ShiftUpdateDTO(
        String officeName,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isActive
) {
}
