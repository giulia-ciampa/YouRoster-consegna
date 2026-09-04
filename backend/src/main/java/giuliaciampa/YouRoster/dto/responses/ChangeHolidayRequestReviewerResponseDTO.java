package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeHolidayRequestReviewerResponseDTO(
        UUID id,
        UUID originalRequestId,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate startDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate endDate,
        String employeeNotes,
        String originalRequestEmployeeNotes,
        LocalDate startDateOriginalRequest,
        LocalDate endDateOriginalRequest,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime originalRequestCreatedAt,
        RequestStatus changeRequestStatus,
        int reviewCount,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate responseDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalResponseDate,
        String reviewerNotes,
        String reviewerName,
        AssignmentType assignmentType,
        Integer totalDays,
        Integer originalRequestTotalDays,
        String originalReviewerNotes
) {
}
