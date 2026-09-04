package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.LeaveHoursType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ChangeLeaveHoursRequestResponseDTO(
        UUID id,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String employeeNotes,
        String originalRequestEmployeeNotes,
        LocalTime startTimeOriginalRequest,
        LocalTime endTimeOriginalRequest,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime originalRequestCreatedAt,
        RequestStatus changeRequestStatus,
        int reviewCount,
        BigDecimal totalHours,
        BigDecimal originalRequestTotalHours,
        String reviewerName,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate responseDate,
        LeaveHoursType leaveHoursType,
        String employeeName,
        String reviewerNotes,
        String originalReviewerNotes,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalResponseDate,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate originalDate
) {
}
