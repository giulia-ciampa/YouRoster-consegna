package giuliaciampa.YouRoster.dto.responses;

import java.util.UUID;

public record UserForShiftAssignmentDTO(
        UUID userId,
        String name

) {
}
