package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HolidayRequestReviewerResponseDTO(

        UUID id,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        String employeeNotes,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        RequestStatus requestStatus,
        LocalDate responseDate,
        String reviewerNotes,
        String reviewer,
        List<String> reviewerRole,
        AssignmentType assignmentType,
        String employeeName,
        Integer totalDays
) {
}
