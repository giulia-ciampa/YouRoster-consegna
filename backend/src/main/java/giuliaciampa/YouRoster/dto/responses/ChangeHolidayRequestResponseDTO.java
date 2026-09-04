package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChangeHolidayRequestResponseDTO(
        UUID id,
        UUID originalRequestId,
        LocalDate startDate,
        LocalDate endDate,
        String employeeNotes,
        String originalRequestEmployeeNotes,
        LocalDate startDateOriginalRequest,
        LocalDate endDateOriginalRequest,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime originalRequestCreatedAt,
        Integer totalDays,
        Integer originalRequestTotalDays,
        RequestStatus changeRequestStatus,
        int reviewCount,
        String reviewerName,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate responseDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalResponseDate,
        String employeeName,
        String reviewerNotes,
        String originalReviewerNotes


) {
}
