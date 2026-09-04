package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AssignmentType;
import giuliaciampa.YouRoster.entities.LeaveHoursType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record LeaveHoursRequestReviewerResponseDTO(
        UUID id,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        LeaveHoursType leaveHoursType,
        String employeeNotes,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        LocalDate responseDate,
        RequestStatus requestStatus,
        String reviewerNotes,
        List<String> reviewerRole,
        String reviewer,
        AssignmentType assignmentType,
        String employeeName,
        BigDecimal totalHours
) {
}
