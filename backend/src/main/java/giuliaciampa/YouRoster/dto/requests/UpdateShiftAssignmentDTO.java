package giuliaciampa.YouRoster.dto.requests;

import giuliaciampa.YouRoster.entities.AssignmentType;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateShiftAssignmentDTO(
        UUID userId,
        UUID shiftId,
        LocalDate shiftDate,
        AssignmentType assignmentType
) {
}
