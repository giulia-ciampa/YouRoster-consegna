package giuliaciampa.YouRoster.dto.requests;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftTaskUpdateRequestDTO(
        LocalTime time,
        UUID taskTitle) {
}
