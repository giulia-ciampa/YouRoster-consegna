package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AssignmentType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ShiftAssignmentResponseDTO(
        UUID id,
        UUID userId,
        List<String> roleNames,
        String userName,
        String userSurname,
        String userEmail,
        String officeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate shiftDate,
        AssignmentType assignmentType,
        List<String> tasks,
        UUID shiftId
) {
}
