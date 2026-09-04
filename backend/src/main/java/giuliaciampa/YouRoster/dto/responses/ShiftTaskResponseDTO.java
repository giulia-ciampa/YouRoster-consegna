package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ShiftTaskResponseDTO(
        UUID id,
        UUID shiftAssignmentId,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate shiftDate,
        String assignmentType,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        UUID taskTitleId,
        String taskTitleName

) {
}
